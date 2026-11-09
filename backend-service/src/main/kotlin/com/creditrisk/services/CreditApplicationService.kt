package com.creditrisk.services

import com.creditrisk.dataSources.CreditApplicationRepository
import com.creditrisk.dataSources.CreditDecisionRepository
import com.creditrisk.exception.ConflictException
import com.creditrisk.exception.NotFoundException
import com.creditrisk.model.ApplicationEntity
import com.creditrisk.model.CreditApplication
import com.creditrisk.model.DecisionEntity
import com.creditrisk.model.DecisionResponse
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

@Service
class CreditApplicationService(
    private val creditApplicationRepository: CreditApplicationRepository,
    private val creditDecisionRepository: CreditDecisionRepository,
    private val mlWebClient: WebClient
) {

    fun saveApplication(entity: ApplicationEntity): ApplicationEntity {
        return creditApplicationRepository.save(entity)
    }

    fun getAllApplications(): List<ApplicationEntity> {
        return creditApplicationRepository.findAll()
    }

    fun getApplication(creditApplicationId: String): ApplicationEntity? {
        return creditApplicationRepository.findByCreditApplicationId(creditApplicationId)
    }

    @Transactional
    fun updateApplication(creditApplicationId: String, updatedData: CreditApplication): ApplicationEntity {
        val existing = getApplication(creditApplicationId)
            ?: throw NotFoundException("Application with ID $creditApplicationId not found")
        existing.updateFrom(updatedData)
        return existing
    }

    @Transactional
    fun deleteByInternalId(id: Long) {
        if (!creditApplicationRepository.existsById(id)) {
            throw NotFoundException("Application with internal ID $id not found")
        }
        creditApplicationRepository.deleteById(id)
    }

    @Transactional
    fun deleteByBusinessId(businessId: String) {
        if (!creditApplicationRepository.existsByCreditApplicationId(businessId)) {
            throw NotFoundException("Application with business ID $businessId not found")
        }
        creditApplicationRepository.deleteByCreditApplicationId(businessId)
    }

    private fun callPythonModel(application: CreditApplication): DecisionResponse? {
        return mlWebClient.post()
            .uri("/predict")
            .bodyValue(application)
            .retrieve()
            .bodyToMono(DecisionResponse::class.java)
            .block()
    }

    @Transactional
    fun processApplication(application: CreditApplication): DecisionResponse {
        val requestId = application.creditApplicationId?.takeIf { it.isNotBlank() }
        val existing = requestId?.let { getApplication(it) }

        // Re-scoring an existing application is allowed only with unchanged inputs
        if (existing != null && !existing.hasSameInputsAs(application)) {
            throw ConflictException(
                "Application $requestId already exists with different data. " +
                    "Update it with PUT /api/credit/applications/$requestId or submit without an ID."
            )
        }

        val appEntity = existing ?: saveApplication(application.toEntity())

        val applicationForModel = appEntity.toDto().copy(decisions = emptyList())

        val response = callPythonModel(applicationForModel)
            ?: throw IllegalStateException("ML service returned an empty response")

        val savedDecision = creditDecisionRepository.save(
            DecisionEntity(
                application = appEntity,
                probability = response.probability,
                isRisky = response.isRisky,
                decision = response.decision,
                modelVersion = response.modelVersion,
                createdAt = LocalDateTime.now()
            )
        )

        return savedDecision.toDto()
    }
}
