package com.creditrisk.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.Table

@Entity
@Table(name = "applications")
class ApplicationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false, name = "application_id")
    var creditApplicationId: String = "",
    var age: Int,
    var monthlyIncome: Double?,
    var debtRatio: Double,
    var revolvingUtilization: Double,
    var pastDue3059: Int,
    var pastDue6089: Int,
    var pastDue90Plus: Int,
    var realEstateLoans: Int,
    var numberOfDependents: Int?,
    var openCreditLines: Int,

    @OneToMany(mappedBy = "application", cascade = [CascadeType.ALL])
    val decisions: MutableList<DecisionEntity> = mutableListOf()
) {
    @PrePersist
    fun ensureBusinessId() {
        if (this.creditApplicationId.isBlank()) {
            val uniqueToken = java.util.UUID.randomUUID().toString().substring(0, 8)
            this.creditApplicationId = "APP-$uniqueToken"
        }
    }

    fun updateFrom(dto: CreditApplication) {
        age = dto.age
        monthlyIncome = dto.monthlyIncome
        debtRatio = dto.debtRatio
        revolvingUtilization = dto.revolvingUtilization
        pastDue3059 = dto.pastDue3059
        pastDue6089 = dto.pastDue6089
        pastDue90Plus = dto.pastDue90Plus
        realEstateLoans = dto.realEstateLoans
        numberOfDependents = dto.numberOfDependents
        openCreditLines = dto.openCreditLines
    }

    fun hasSameInputsAs(dto: CreditApplication): Boolean =
        age == dto.age &&
            monthlyIncome == dto.monthlyIncome &&
            debtRatio == dto.debtRatio &&
            revolvingUtilization == dto.revolvingUtilization &&
            pastDue3059 == dto.pastDue3059 &&
            pastDue6089 == dto.pastDue6089 &&
            pastDue90Plus == dto.pastDue90Plus &&
            realEstateLoans == dto.realEstateLoans &&
            numberOfDependents == dto.numberOfDependents &&
            openCreditLines == dto.openCreditLines

    fun toDto(): CreditApplication {
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
            numberOfDependents = this.numberOfDependents,
            decisions = this.decisions.map { it.toDto() }
        )
    }
}
