import io
import logging
import os
import sys
from contextlib import asynccontextmanager
from typing import Optional

import pandas as pd
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.responses import JSONResponse

from app.schemas import CreditApplication
from src.models.model import DEFAULT_THRESHOLD, MODEL_VERSION, Model

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
model_pipeline: Optional[Model] = None

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
logger = logging.getLogger("ml-service")


COLUMNS_ORDER = [
    "RevolvingUtilizationOfUnsecuredLines",
    "age",
    "NumberOfTime30-59DaysPastDueNotWorse",
    "DebtRatio",
    "MonthlyIncome",
    "NumberOfOpenCreditLinesAndLoans",
    "NumberOfTimes90DaysLate",
    "NumberRealEstateLoansOrLines",
    "NumberOfTime60-89DaysPastDueNotWorse",
    "NumberOfDependents",
]


@asynccontextmanager
async def lifespan(app: FastAPI):
    global model_pipeline
    model_path = os.path.join(BASE_DIR, "models", "xgb_model.joblib")

    if not os.path.exists(model_path):
        logger.error(f"Model file not found at {model_path}")
    else:
        try:
            model_pipeline = Model.load_model(model_path)
            logger.info(f"Model loaded successfully (version={MODEL_VERSION}, threshold={DEFAULT_THRESHOLD})")
        except Exception:
            logger.exception("Failed to load model")

    yield
    logger.info("Shutting down...")


app = FastAPI(
    lifespan=lifespan,
    title="CreditRiskPredictor",
)


def _require_model() -> Model:
    if model_pipeline is None:
        raise HTTPException(status_code=503, detail="Model not loaded")
    return model_pipeline


def _to_result(application_id: str, probability: float) -> dict:
    is_risky = probability > DEFAULT_THRESHOLD
    return {
        "credit_application_id": application_id,
        "probability": round(probability, 4),
        "is_risky": is_risky,
        "decision": "REJECT" if is_risky else "APPROVE",
        "model_version": MODEL_VERSION,
    }


def _score(df: pd.DataFrame) -> list[float]:
    model = _require_model()
    try:
        return [float(p) for p in model.predict_proba(df[COLUMNS_ORDER])]
    except Exception as e:
        logger.exception("Prediction failed")
        raise HTTPException(status_code=500, detail=f"Prediction error: {e}")


@app.get("/health")
def health_check():
    loaded = model_pipeline is not None
    body = {"status": "alive" if loaded else "unstable", "model_loaded": loaded, "model_version": MODEL_VERSION}
    return JSONResponse(content=body, status_code=200 if loaded else 503)


@app.post("/predict")
def predict(payload: CreditApplication):
    raw = payload.model_dump(by_alias=True, exclude={"credit_application_id"})
    probability = _score(pd.DataFrame([raw]))[0]
    return _to_result(payload.credit_application_id, probability)


@app.post("/predict-batch")
def predict_batch(payload: list[CreditApplication]):
    if not payload:
        return []
    rows = [item.model_dump(by_alias=True, exclude={"credit_application_id"}) for item in payload]
    probabilities = _score(pd.DataFrame(rows))
    return [_to_result(item.credit_application_id, p) for item, p in zip(payload, probabilities)]


@app.post("/predict-csv")
async def predict_csv(file: UploadFile = File(...)):
    _require_model()

    if not file.filename or not file.filename.lower().endswith(".csv"):
        raise HTTPException(status_code=400, detail="Only .csv files are allowed")

    try:
        df = pd.read_csv(io.BytesIO(await file.read()))
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Could not parse CSV: {e}")

    missing_cols = [c for c in COLUMNS_ORDER if c not in df.columns]
    if missing_cols:
        raise HTTPException(status_code=400, detail=f"Missing columns in CSV: {missing_cols}")

    probabilities = _score(df)
    ids = df["credit_application_id"].astype(str) if "credit_application_id" in df.columns else [f"row_{i}" for i in range(len(df))]
    return [_to_result(app_id, p) for app_id, p in zip(ids, probabilities)]


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)
