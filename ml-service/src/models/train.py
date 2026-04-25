from skopt import BayesSearchCV
from sklearn.model_selection import train_test_split
from skopt.space import Real, Integer
from xgboost import XGBClassifier
from model import Model
import pandas as pd
from sklearn.pipeline import Pipeline
from transformers import Preprocessor, FeatureEngineer
from sklearn.metrics import roc_auc_score, recall_score, confusion_matrix, precision_score, f1_score
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

def evaluate_model(model, X_test, y_test, model_name="Model", threshold=0.39):
    y_probs = model.predict_proba(X_test)
    # Using 0.5 as the default threshold
    y_pred = (y_probs > threshold).astype(int)

    roc_auc = roc_auc_score(y_test, y_probs)
    recall = recall_score(y_test, y_pred)
    precision = precision_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)

    cm = confusion_matrix(y_test, y_pred)
    tn, fp, fn, tp = cm.ravel()

    print(f"\n{'=' * 15} {model_name.upper()} EVALUATION {'=' * 15}")

    if hasattr(model, 'best_score_'):
        print(f"Best CV Score (AUC): {model.best_score_:.4f}")

    print(f"Test AUC-ROC:      {roc_auc:.4f}")
    print(f"Test Recall:       {recall:.4f} (Sensitivity/Default Detection)")
    print(f"Test Precision:    {precision:.4f} (Positive Predictive Value)")
    print(f"Test F1-Score:     {f1:.4f}")

    print("\nCONFUSION MATRIX:")
    print(f"{'':>18} | {'Model: SAFE (0)':>15} | {'Model: RISKY (1)':>15}")
    print(f"{'-' * 55}")
    print(f"{'Actual: SAFE (0)':>18} | {tn:>15} | {fp:>15}")
    print(f"{'Actual: RISKY (1)':>18} | {fn:>15} | {tp:>15}")

    print("\nBUSINESS INSIGHTS:")
    print(f"- False Positives (Safe customers rejected): {fp:>9}")
    print(f"- False Negatives (Risky debtors accepted):  {fn:>9}")
    print(f"- Default Capture Rate: {recall * 100:.2f}%")
    print("=" * 50)

def run_training():
    data_path = os.path.join(BASE_DIR, "data", "raw", "cs-training.csv")

    df = pd.read_csv(data_path)
    df = df.drop(columns=["Unnamed: 0"])

    X, y = df.drop('SeriousDlqin2yrs', axis=1), df['SeriousDlqin2yrs']
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, stratify=y, random_state=42)

    pipeline = Pipeline(steps=[
        ('preprocessing', Preprocessor()),
        ('feature_eng', FeatureEngineer()),
        ('xgb_clf', XGBClassifier(
            random_state=42
        ))
    ])

    search_spaces = {
        'xgb_clf__n_estimators': Integer(1, 300),
        'xgb_clf__max_depth': Integer(3, 10),
        'xgb_clf__learning_rate': Real(0.01, 1, prior='log-uniform'),
        'xgb_clf__subsample': Real(0.5, 1.0),
        'xgb_clf__scale_pos_weight': Integer(10, 20),
        'xgb_clf__colsample_bytree': Real(0.5, 1.0),
        'xgb_clf__colsample_bylevel': Real(0.5, 1.0),
        'xgb_clf__colsample_bynode': Real(0.5, 1.0),
        'xgb_clf__reg_alpha': Real(0.0, 10.0),
        'xgb_clf__reg_lambda': Real(0.0, 10.0),
        'xgb_clf__gamma': Real(0.0, 10.0)
    }

    opt = BayesSearchCV(
        pipeline,
        search_spaces,
        n_iter=128,
        cv=5,
        scoring='roc_auc',
        n_jobs=-1,
        random_state=42
    )

    opt.fit(X_train, y_train)

    final_model = Model(pipeline=opt.best_estimator_)
    final_model.train_final(X_train, y_train)

    model_save_dir = os.path.join(BASE_DIR, "models")
    os.makedirs(model_save_dir, exist_ok=True)

    model_save_path = os.path.join(model_save_dir, "xgb_model.joblib")
    final_model.save_model(model_save_path)

    evaluate_model(final_model, X_test, y_test, model_name='xgb_model', threshold=0.39)

if __name__ == "__main__":
    run_training()