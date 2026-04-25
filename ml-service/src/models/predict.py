import pandas as pd
from models.model import Model
import os
from src.models.model import Model

threshold = 0.39
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

def run_prediction(input_data_path=None, output_path=None):
    if input_data_path is None:
        input_data_path = os.path.join(BASE_DIR, 'data', 'raw', 'cs-test.csv')

    if output_path is None:
        output_path = os.path.join(BASE_DIR, 'predictions', 'predictions_results.csv')

    model_path = os.path.join(BASE_DIR, 'models', 'xgb_model.joblib')
    new_data = pd.read_csv(input_data_path)

    # 'Unnamed: 0' may appear when a DataFrame index was accidentally saved to CSV
    # (e.g., pandas default behavior). It should be removed before prediction.
    # If dataset contains an 'Id' column, preserve it for output
    # (do not use it as a feature for the model).
    # Example:
    # ids = new_data['Id']
    ids = new_data['Unnamed: 0'] if 'Unnamed: 0' in new_data.columns else None

    model = Model.load_model(model_path)
    drop_cols = ['Unnamed: 0', 'SeriousDlqin2yrs']
    new_data = new_data.drop(columns=[col for col in drop_cols if col in new_data.columns])

    probabilities = model.predict_proba(new_data)

    predictions = (probabilities > threshold).astype(int)

    results = pd.DataFrame({
        'Id': ids,
        'probability': probabilities,
        'prediction_default': predictions
    })

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    results.to_csv(output_path, index=False)
    print(f"Predictions saved to: {output_path}")


if __name__ == "__main__":
    run_prediction()