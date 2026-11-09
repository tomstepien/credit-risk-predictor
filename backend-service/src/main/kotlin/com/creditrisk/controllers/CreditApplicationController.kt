package com.creditrisk.controllers

import com.creditrisk.exception.NotFoundException
import com.creditrisk.model.CreditApplication
import com.creditrisk.model.DecisionResponse
import com.creditrisk.services.CreditApplicationService
import jakarta.validation.Valid
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/credit/applications")
class CreditApplicationController(
    private val creditApplicationService: CreditApplicationService,
    private val healthEndpoint: HealthEndpoint
) {
    @PostMapping("/predict")
    fun predict(
        @Valid @RequestBody application: CreditApplication
    ): DecisionResponse {
        return creditApplicationService.processApplication(application)
    }

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Map<String, String>> {
        val dbStatus = healthEndpoint.healthForPath("db")?.status?.code ?: "UNKNOWN"
        return ResponseEntity.ok(mapOf(
            "service" to "Backend Service",
            "status" to "Running",
            "database" to dbStatus
        ))
    }

    @GetMapping("/all")
    fun getAllApplications(): List<CreditApplication> {
        return creditApplicationService.getAllApplications().map { it.toDto() }
    }

    @GetMapping("/{id}")
    fun getApplication(@PathVariable id: String): CreditApplication {
        return creditApplicationService.getApplication(id)?.toDto()
            ?: throw NotFoundException("Application not found")
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createApplication(@Valid @RequestBody app: CreditApplication): CreditApplication {
        return creditApplicationService.saveApplication(app.toEntity()).toDto()
    }

    @PutMapping("/{id}")
    fun updateApplication(
        @PathVariable id: String,
        @Valid @RequestBody updatedData: CreditApplication
    ): CreditApplication {
        return creditApplicationService.updateApplication(id, updatedData).toDto()
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
