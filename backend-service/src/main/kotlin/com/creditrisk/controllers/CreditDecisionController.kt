package com.creditrisk.controllers

import com.creditrisk.model.DecisionResponse
import com.creditrisk.services.CreditDecisionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/credit/decisions")
class CreditDecisionController(
    private val creditDecisionService: CreditDecisionService
) {

    @GetMapping("/all")
    fun getAllDecisions(): List<DecisionResponse> {
        return creditDecisionService.getAllDecisions().map { it.toDto() }
    }

    @GetMapping("/application/{applicationId}")
    fun getDecisionsForApplication(@PathVariable applicationId: String): List<DecisionResponse> {
        return creditDecisionService.getDecisionsForApplication(applicationId).map { it.toDto() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDecision(@PathVariable id: Long) {
        creditDecisionService.deleteDecision(id)
    }
}