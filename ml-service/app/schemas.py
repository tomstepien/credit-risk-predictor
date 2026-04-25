from pydantic import BaseModel, Field, field_validator, model_validator
from typing import Optional


class CreditApplication(BaseModel):
    credit_application_id: str
    revolving_utilization: float = Field(alias="RevolvingUtilizationOfUnsecuredLines")
    age: int
    past_due_30_59: int = Field(alias="NumberOfTime30-59DaysPastDueNotWorse")
    past_due_60_89: int = Field(alias="NumberOfTime60-89DaysPastDueNotWorse")
    past_due_90_plus: int = Field(alias="NumberOfTimes90DaysLate")
    debt_ratio: float = Field(alias="DebtRatio")
    monthly_income: Optional[float] = Field(alias="MonthlyIncome", default=None)
    open_credit_lines: int = Field(alias="NumberOfOpenCreditLinesAndLoans")
    real_estate_loans: int = Field(alias="NumberRealEstateLoansOrLines")
    number_of_dependents: Optional[int] = Field(alias="NumberOfDependents", default=None)

    @field_validator('age')
    @classmethod
    def age_validator(cls, v: int):
        if v < 18 or v > 90:
            raise ValueError("Age must be between 18 and 90")
        return v

    @model_validator(mode='after')
    def check_non_negative(self) -> 'CreditApplication':
        data = self.model_dump()

        for field_name, value in data.items():
            if field_name == "credit_application_id":
                continue

            if value is not None and isinstance(value, (int, float)) and value < 0:
                alias = self.model_fields[field_name].alias or field_name
                raise ValueError(f"Value for field '{alias}' cannot be negative (provided: {value}).")

        return self

    class Config:
        populate_by_name = True