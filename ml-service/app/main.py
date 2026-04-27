import os
import logging
import sys
import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException, UploadFile, File
import io
from app.schemas import CreditApplication
from src.models.model import Model
import src.models.predict as predict_module
from contextlib import asynccontextmanager
from typing import Optional
import uvicorn
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
model_pipeline: Optional[Model] = None

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
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
    "NumberOfDependents"
]

@asynccontextmanager
async def lifespan(app: FastAPI):
    global model_pipeline
    model_path = os.path.join(BASE_DIR, "models", "xgb_model.joblib")

    if not os.path.exists(model_path):
        logger.info(f"❌ CRITICAL: Model file not found at {model_path}")
    else:
        try:
            model_pipeline = Model.load_model(model_path)
            logger.info("🚀 Model loaded successfully!")
        except Exception as e:
            logger.info(f"❌ Failed to load model: {e}")

    yield
    logger.info("Shutting down...")

app = FastAPI(
    lifespan=lifespan,
    title="CreditRiskPredictor",
)

@app.get("/health")
def health_check():
    return {
        "status": "alive" if model_pipeline else "unstable",
        "model_loaded": model_pipeline is not None
    }

@app.post("/predict")
def predict(payload: CreditApplication):
    if model_pipeline is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    try:
        raw_data = payload.model_dump(by_alias=True, exclude={"credit_application_id"})

        df = pd.DataFrame([raw_data])
        df = df[COLUMNS_ORDER]

        probability = model_pipeline.predict_proba(df)
        prob_value = float(probability[0])

        return {
            "credit_application_id": payload.credit_application_id,
            "probability": round(prob_value, 4),
            "is_risky": bool(prob_value > predict_module.threshold),
            "decision": "REJECT" if prob_value > predict_module.threshold else "APPROVE"
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

@app.post("/predict-batch")
def predict_batch(payload: list[CreditApplication]):
    if model_pipeline is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    try:
        raw_data_list = [data.model_dump(by_alias=True, exclude={"credit_application_id"}) for data in payload]

        df = pd.DataFrame(raw_data_list)
        df = df[COLUMNS_ORDER]

        probabilities = model_pipeline.predict_proba(df)

        results = []
        for i, probability in enumerate(probabilities):
            prob_val = float(probability)
            results.append({
                "credit_application_id": payload[i].credit_application_id,
                "probability": round(prob_val, 4),
                "is_risky": bool(prob_val > predict_module.threshold),
                "decision": "REJECT" if prob_val > predict_module.threshold else "APPROVE"
            })

        return results

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

@app.post("/predict-csv")
async def predict_csv(file: UploadFile = File(...)):
    if model_pipeline is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    if not file.filename.endswith(".csv"):
        raise HTTPException(status_code=400, detail="File extension not allowed")

    try:
        contents = await file.read()
        df = pd.read_csv(io.BytesIO(contents))

        missing_cols = [c for c in COLUMNS_ORDER if c not in df.columns]
        if missing_cols:
            raise ValueError(f"Missing columns in CSV: {missing_cols}")

        X = df[COLUMNS_ORDER]

        probabilities = model_pipeline.predict_proba(X)

        results = []
        for i, probability in enumerate(probabilities):
            app_id = df.iloc[i].get('credit_application_id', f"row_{i}")
            prob_val = float(probability)
            
            results.append({
                "credit_application_id": str(app_id),
                "probability": round(prob_val, 4),
                "is_risky": bool(prob_val > predict_module.threshold),
                "decision": "REJECT" if prob_val > predict_module.threshold else "APPROVE"
            })

        return results

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)