package com.creditrisk.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero

data class CreditApplication(
    @get:JsonProperty("credit_application_id")
    val creditApplicationId: String? = null,

    @get:JsonProperty("RevolvingUtilizationOfUnsecuredLines")
    @field:PositiveOrZero
    val revolvingUtilization: Double,

    @field:Min(18)
    @field:Max(90)
    val age: Int,

    @get:JsonProperty("NumberOfTime30-59DaysPastDueNotWorse")
    @field:PositiveOrZero
    val pastDue3059: Int,

    @get:JsonProperty("NumberOfTime60-89DaysPastDueNotWorse")
    @field:PositiveOrZero
    val pastDue6089: Int,

    @get:JsonProperty("NumberOfTimes90DaysLate")
    @field:PositiveOrZero
    val pastDue90Plus: Int,

    @get:JsonProperty("DebtRatio")
    @field:PositiveOrZero
    val debtRatio: Double,

    @get:JsonProperty("MonthlyIncome")
    @field:PositiveOrZero
    val monthlyIncome: Double? = null,

    @get:JsonProperty("NumberOfOpenCreditLinesAndLoans")
    @field:PositiveOrZero
    val openCreditLines: Int,

    @get:JsonProperty("NumberRealEstateLoansOrLines")
    @field:PositiveOrZero
    val realEstateLoans: Int,

    @get:JsonProperty("NumberOfDependents")
    @field:PositiveOrZero
    val numberOfDependents: Int? = null
){
    fun toEntity(): ApplicationEntity {
        return ApplicationEntity(
            creditApplicationId = this.creditApplicationId ?: "",
            revolvingUtilization = this.revolvingUtilization,
            age = this.age,
            pastDue3059 = this.pastDue3059,
            pastDue6089 = this.pastDue6089,
            pastDue90Plus = this.pastDue90Plus,
            debtRatio = this.debtRatio,
            monthlyIncome = this.monthlyIncome,
            openCreditLines = this.openCreditLines,
            realEstateLoans = this.realEstateLoans,
            numberOfDependents = this.numberOfDependents
        )
    }

}