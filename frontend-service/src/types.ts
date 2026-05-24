export interface CreditApplication {
    credit_application_id?: string;
    RevolvingUtilizationOfUnsecuredLines: number;
    age: number;
    "NumberOfTime30-59DaysPastDueNotWorse": number;
    "NumberOfTime60-89DaysPastDueNotWorse": number;
    NumberOfTimes90DaysLate: number;
    DebtRatio: number;
    MonthlyIncome: number | null;
    NumberOfOpenCreditLinesAndLoans: number;
    NumberRealEstateLoansOrLines: number;
    NumberOfDependents: number | null;
}

export interface PredictionResult {
    decision: string;
    probability: number;
    isRisky: boolean;
    modelVersion: string;
    creditApplicationId: string;
}

export interface FormErrors {
    age?: string;
    MonthlyIncome?: string;
    RevolvingUtilizationOfUnsecuredLines?: string;
    DebtRatio?: string;
    NumberOfOpenCreditLinesAndLoans?: string;
    NumberRealEstateLoansOrLines?: string;
    NumberOfDependents?: string;
    "NumberOfTime30-59DaysPastDueNotWorse"?: string;
    "NumberOfTime60-89DaysPastDueNotWorse"?: string;
    NumberOfTimes90DaysLate?: string;
}