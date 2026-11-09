package com.creditrisk.services

import com.creditrisk.dataSources.CreditDecisionRepository
import com.creditrisk.exception.NotFoundException
import com.creditrisk.model.DecisionEntity
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CreditDecisionService(
    private val creditDecisionRepository: CreditDecisionRepository
) {
    fun getAllDecisions(): List<DecisionEntity> = creditDecisionRepository.findAll()

    fun getDecisionsForApplication(appId: String): List<DecisionEntity> {
        return creditDecisionRepository.findAllDecisionsByApplicationId(appId)
    }

    @Transactional
    fun deleteDecision(id: Long) {
        if (!creditDecisionRepository.existsById(id)) {
            throw NotFoundException("Decision with ID $id not found")
        }
        creditDecisionRepository.deleteById(id)
    }
}
