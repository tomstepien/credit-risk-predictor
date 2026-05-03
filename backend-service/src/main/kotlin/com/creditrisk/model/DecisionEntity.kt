package com.creditrisk.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "decisions")
class DecisionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "application_internal_id")
    val application: ApplicationEntity,

    val probability: Double,
    val isRisky: Boolean,
    val decision: String,
    val modelVersion: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
){
    fun toDto(): DecisionResponse{
        return DecisionResponse(
            creditApplicationId = this.application.creditApplicationId,
            decision = this.decision,
            modelVersion = this.modelVersion,
            probability = this.probability,
            isRisky = this.isRisky,
            createdAt = this.createdAt
        )
    }
}