import pandas as pd
import numpy as np

def clean_age(df) -> pd.DataFrame:
    df.loc[df['age'] == 0, 'age'] = df['age'].median()
    return df

def fill_missing_values(df) -> pd.DataFrame:
    # Filling NumberOfDependents
    df['NumberOfDependents'] = df['NumberOfDependents'].fillna(0).astype(np.int64)

    # Filling MonthlyIncome
    working = df[(df['age'] >= 18) & (df['age'] < 60)]
    senior = df[df['age'] >= 60]

    df['MonthlyIncome'] = df['MonthlyIncome'].fillna(-1)

    df.loc[(df['MonthlyIncome'] == -1) & (df['age'] < 60), 'MonthlyIncome'] = working['MonthlyIncome'].mean()
    df.loc[(df['MonthlyIncome'] == -1) & (df['age'] >= 60), 'MonthlyIncome'] = senior['MonthlyIncome'].mean()

    return df

def preprocess(df) -> pd.DataFrame:
    df = df.copy()
    df = clean_age(df)
    df = fill_missing_values(df)
    return df