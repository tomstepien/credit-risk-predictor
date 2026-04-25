import pandas as pd
import numpy as np
from sklearn.base import BaseEstimator, TransformerMixin


class Preprocessor(BaseEstimator, TransformerMixin):
    def __init__(self):
        self.median_age = None
        self.working_mean_income = None
        self.senior_mean_income = None

    def fit(self, X, y=None):
        self.median_age = X['age'].median()
        self.working_mean_income = X[X['age'] < 60]['MonthlyIncome'].mean()
        self.senior_mean_income = X[X['age'] >= 60]['MonthlyIncome'].mean()
        return self

    def transform(self, X):
        X_copy = X.copy()
        if 'NumberOfDependents' in X_copy.columns:
            X_copy['NumberOfDependents'] = X_copy['NumberOfDependents'].fillna(0).astype(np.int64)

        X_copy.loc[X_copy['age'] == 0, 'age'] = self.median_age
        X_copy['MonthlyIncome'] = X_copy['MonthlyIncome'].fillna(-1)
        X_copy.loc[(X_copy['MonthlyIncome'] == -1) & (X_copy['age'] < 60), 'MonthlyIncome'] = self.working_mean_income
        X_copy.loc[(X_copy['MonthlyIncome'] == -1) & (X_copy['age'] >= 60), 'MonthlyIncome'] = self.senior_mean_income
        return X_copy