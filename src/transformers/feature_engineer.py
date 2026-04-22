import pandas as pd
import numpy as np
from sklearn.base import BaseEstimator, TransformerMixin

class FeatureEngineer(BaseEstimator, TransformerMixin):
    def fit(self, X, y=None):
        return self

    def transform(self, X):
        X_copy = X.copy()
        X_copy['WasEverLate'] = (
            X_copy['NumberOfTime30-59DaysPastDueNotWorse'] +
            X_copy['NumberOfTime60-89DaysPastDueNotWorse'] +
            X_copy['NumberOfTimes90DaysLate']
        )
        X_copy['WasEverLate'] = (X_copy['WasEverLate'] >= 1).astype(np.int64)

        X_copy['DebtOnAssets'] = (
            X_copy['NumberOfOpenCreditLinesAndLoans'] +
            X_copy['NumberRealEstateLoansOrLines']
        )
        X_copy['DebtOnAssets'] = (X_copy['DebtOnAssets'] >= 6).astype(np.int64)

        cols_to_drop = [
            'NumberOfTime30-59DaysPastDueNotWorse', 'NumberOfTime60-89DaysPastDueNotWorse',
            'NumberOfTimes90DaysLate', 'NumberRealEstateLoansOrLines', 'NumberOfOpenCreditLinesAndLoans'
        ]

        return X_copy.drop(columns=cols_to_drop, errors='ignore')