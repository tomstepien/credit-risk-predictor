package com.creditrisk.services

import com.creditrisk.dataSources.CreditApplicationRepository
import com.creditrisk.dataSources.CreditDecisionRepository
import com.creditrisk.model.ApplicationEntity
import com.creditrisk.model.CreditApplication
import com.creditrisk.model.DecisionEntity
import com.creditrisk.model.DecisionResponse
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

@Service
class CreditApplicationService(
    private val creditApplicationRepository: CreditApplicationRepository,
    private val creditDecisionRepository: CreditDecisionRepository,
    private val webClientBuilder: WebClient.Builder,
    @Value("\${ML_SERVICE_URL:http://localhost:8000}")
    private val mlServiceUrl: String
) {
    private val client = webClientBuilder.baseUrl(mlServiceUrl).build()



    fun saveApplication(entity: ApplicationEntity): ApplicationEntity {
        entity.creditApplicationId = "APP-${System.currentTimeMillis()}"

        return creditApplicationRepository.save(entity)
    }

    fun getAllApplications(): List<ApplicationEntity> {
        return creditApplicationRepository.findAll()
    }

    fun getApplication(creditApplicationId: String): ApplicationEntity? {
        return creditApplicationRepository.findByCreditApplicationId(creditApplicationId)
    }

    @Transactional
    fun updateApplication(creditApplicationId: String, updatedData: ApplicationEntity): ApplicationEntity? {
        if (!creditApplicationRepository.existsByCreditApplicationId(creditApplicationId)) {
            throw RuntimeException(
                "Application with ID $creditApplicationId not found"
            )
        }

        return creditApplicationRepository.save(updatedData)
    }

    @Transactional
    fun deleteByInternalId(id: Long) {
        if (!creditApplicationRepository.existsById(id)) {
            throw EntityNotFoundException("Application with internal ID $id not found")
        }
        creditApplicationRepository.deleteById(id)
    }

    @Transactional
    fun deleteByBusinessId(businessId: String) {
        if (!creditApplicationRepository.existsByCreditApplicationId(businessId)) {
            throw EntityNotFoundException("Application with business ID $businessId not found")
        }
        creditApplicationRepository.deleteByCreditApplicationId(businessId)
    }

    private fun callPythonModel(application: CreditApplication): DecisionResponse? {
        return client.post()
            .uri("/predict")
            .bodyValue(application)
            .retrieve()
            .bodyToMono(DecisionResponse::class.java)
            .block()
    }

    @Transactional
    fun processApplication(application: CreditApplication): DecisionResponse {
        val requestId = application.creditApplicationId

        val appEntity = if (requestId != null) {
            getApplication(requestId) ?: saveApplication(application.toEntity())
        } else {
            saveApplication(application.toEntity())
        }

        val applicationForModel = application.copy(creditApplicationId = appEntity.creditApplicationId)

        val response = callPythonModel(applicationForModel)
            ?: throw RuntimeException("FastAPI returned no response")

        val savedDecision = creditDecisionRepository.save(
            DecisionEntity(
                application = appEntity,
                probability = response.probability,
                isRisky = response.isRisky,
                decision = response.decision,
                modelVersion = "xgb_v1",
                createdAt = LocalDateTime.now()
            )
        )

        return savedDecision.toDto()
    }
}