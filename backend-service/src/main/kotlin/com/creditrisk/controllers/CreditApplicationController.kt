package com.creditrisk.controllers

import com.creditrisk.model.CreditApplication
import com.creditrisk.services.CreditApplicationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.creditrisk.model.DecisionResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/credit/applications")
@CrossOrigin(origins = ["*"])
class CreditApplicationController(
    private val creditApplicationService: CreditApplicationService
) {
    @PostMapping("/predict")
    fun predict(
        @Valid @RequestBody application: CreditApplication
    ): DecisionResponse {
        return creditApplicationService.processApplication(application)
    }

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "service" to "Backend Service",
            "status" to "Running",
            "database" to "Connected"
        ))
    }

    @GetMapping("/all")
    fun getAllApplications(): List<CreditApplication> {
        return creditApplicationService.getAllApplications().map { it.toDto() }
    }

    @GetMapping("/{id}")
    fun getApplication(@PathVariable id: String): CreditApplication {
        val app = creditApplicationService.getApplication(id)

        return app?.toDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createApplication(@RequestBody app: CreditApplication): CreditApplication {
        val entityToSave = app.toEntity()

        val savedEntity = creditApplicationService.saveApplication(entityToSave)

        return savedEntity.toDto()
    }

    @PutMapping("/{id}")
    fun updateApplication(
        @PathVariable id: String,
        @RequestBody updatedData: CreditApplication
    ): CreditApplication {
        val entity = updatedData.toEntity()

        val updatedEntity = creditApplicationService.updateApplication(id, entity)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")

        return updatedEntity.toDto()
    }

    @DeleteMapping("/internal/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteInternal(@PathVariable id: Long) {
        creditApplicationService.deleteByInternalId(id)
    }

    @DeleteMapping("/{businessId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBusiness(@PathVariable businessId: String) {
        creditApplicationService.deleteByBusinessId(businessId)
    }
}