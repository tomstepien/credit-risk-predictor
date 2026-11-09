import joblib
import os

DEFAULT_THRESHOLD = 0.39
MODEL_VERSION = os.getenv("MODEL_VERSION", "xgb_v1")


class Model:
    def __init__(self, pipeline=None):
        self.pipeline = pipeline

    def train_final(self, X_train, y_train):
        if self.pipeline is None:
            raise ValueError("Pipeline was not initialized!")
        self.pipeline.fit(X_train, y_train)

    def predict_proba(self, X):
        return self.pipeline.predict_proba(X)[:, 1]

    def predict(self, X, threshold=DEFAULT_THRESHOLD):
        probabilities = self.predict_proba(X)
        return (probabilities > threshold).astype(int)

    def save_model(self, filepath):
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        joblib.dump(self.pipeline, filepath)

    @classmethod
    def load_model(cls, filepath):
        return cls(pipeline=joblib.load(filepath))
