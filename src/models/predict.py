import pandas as pd
from model import Model

threshold = 0.39

def run_prediction(input_data_path, output_path):
    model = Model.load_model('trained_models/xgb_model.joblib')

    new_data = pd.read_csv(input_data_path)

    # 'Unnamed: 0' may appear when a DataFrame index was accidentally saved to CSV
    # (e.g., pandas default behavior). It should be removed before prediction.
    # If dataset contains an 'Id' column, preserve it for output
    # (do not use it as a feature for the model).
    # Example:
    # ids = new_data['Id']
    ids = new_data['Unnamed: 0'] if 'Unnamed: 0' in new_data.columns else None

    drop_cols = ['Unnamed: 0', 'SeriousDlqin2yrs']
    new_data = new_data.drop(columns=[col for col in drop_cols if col in new_data.columns])

    probabilities = model.predict_proba(new_data)

    predictions = (probabilities > threshold).astype(int)

    results = pd.DataFrame({
        'Id': ids,
        'probability': probabilities,
        'prediction_default': predictions
    })

    results.to_csv(output_path, index=False)
    print(f"Predictions saved to: {output_path}")


if __name__ == "__main__":
    run_prediction('../../data/raw/cs-test.csv', '../../predictions/predictions_results.csv')