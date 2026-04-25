import numpy as np
import pandas as pd

def create_was_ever_late(df) -> pd.DataFrame:
    df['WasEverLate'] = (
        df['NumberOfTime30-59DaysPastDueNotWorse'] +
        df['NumberOfTime60-89DaysPastDueNotWorse'] +
        df['NumberOfTimes90DaysLate']
    )
    df['WasEverLate'] = (df['WasEverLate'] >= 1).astype(np.int64)
    return df

def create_debt_on_assets(df) -> pd.DataFrame:
    df['DebtOnAssets'] = (
        df['NumberOfOpenCreditLinesAndLoans'] +
        df['NumberRealEstateLoansOrLines']
    )
    df['DebtOnAssets'] = (df['DebtOnAssets'] >= 6).astype(np.int64)
    return df

def drop_unused_columns(df)-> pd.DataFrame:
    return df.drop(columns=[
        'NumberOfTime30-59DaysPastDueNotWorse',
        'NumberOfTime60-89DaysPastDueNotWorse',
        'NumberOfTimes90DaysLate',
        'NumberRealEstateLoansOrLines',
        'NumberOfOpenCreditLinesAndLoans'
    ])

def build_features(df):
    df = df.copy()
    df = create_was_ever_late(df)
    df = create_debt_on_assets(df)
    df = drop_unused_columns(df)
    return df