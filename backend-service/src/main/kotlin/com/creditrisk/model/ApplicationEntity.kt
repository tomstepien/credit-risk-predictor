package com.creditrisk.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "applications")
data class ApplicationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false, name = "application_id")
    var creditApplicationId: String,
    val age: Int,
    val monthlyIncome: Double?,
    val debtRatio: Double,
    val revolvingUtilization: Double,
    val pastDue3059: Int,
    val pastDue6089: Int,
    val pastDue90Plus: Int,
    val realEstateLoans: Int,
    val numberOfDependents: Int?,
    val openCreditLines: Int,

    @OneToMany(mappedBy = "application", cascade = [CascadeType.ALL])
    val decisions: List<DecisionEntity> = mutableListOf()
){
    fun toDto(): CreditApplication{
        return CreditApplication(
            creditApplicationId = this.creditApplicationId,
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