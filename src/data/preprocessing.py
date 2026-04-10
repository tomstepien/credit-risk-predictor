import pandas as pd
import numpy as np
def fill_missing_values(df) -> pd.DataFrame:
    df['NumberOfDependents'] = df['NumberOfDependents'].fillna(0).astype(np.int64)
    return df

def fit_preprocess(df) -> tuple[int, float, float]:
    median_age = df['age'].median()

    working_mean_income = df[(df['age'] < 60)]['MonthlyIncome'].mean()
    senior_mean_income = df[(df['age'] >= 60)]['MonthlyIncome'].mean()

    return median_age, working_mean_income, senior_mean_income

def transform(df, median_age, working_mean, senior_mean) -> pd.DataFrame:
    df = df.copy()

    df.loc[df['age'] == 0, 'age'] = median_age
    df['MonthlyIncome'] = df['MonthlyIncome'].fillna(-1)

    df.loc[(df['MonthlyIncome'] == -1) & (df['age'] < 60), 'MonthlyIncome'] = working_mean
    df.loc[(df['MonthlyIncome'] == -1) & (df['age'] >= 60), 'MonthlyIncome'] = senior_mean

    return df
