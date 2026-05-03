package com.creditrisk.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDateTime


@ControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = mutableMapOf<String, String>()
        e.bindingResult.fieldErrors.forEach { error ->
            errors[error.field] = error.defaultMessage ?: "Invalid value"
        }

        return ResponseEntity(
            mapOf(
                "timestamp" to LocalDateTime.now(),
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Validation Failed",
                "details" to errors
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(WebClientResponseException::class)
    fun handlePythonServiceError(e: Exception): ResponseEntity<Map<String, Any>> {
        return ResponseEntity(
            mapOf(
                "timestamp" to LocalDateTime.now(),
                "status" to HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error" to "ML Model Service Error",
                "message" to (e.message ?: "FastAPI service is currently unavailable or returned an error.")
            ),
            HttpStatus.SERVICE_UNAVAILABLE
        )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleBusinessLogicError(e: RuntimeException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity(
            mapOf(
                "timestamp" to LocalDateTime.now(),
                "status" to HttpStatus.BAD_GATEWAY.value(),
                "error" to "Business Logic / Integration Error",
                "message" to (e.message ?: "Error during credit application processing")
            ),
            HttpStatus.BAD_GATEWAY
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(e: Exception): ResponseEntity<Map<String, Any>> {
        return ResponseEntity(
            mapOf(
                "timestamp" to LocalDateTime.now(),
                "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error" to "Internal Server Error",
                "message" to (e.message ?: "An unexpected error occurred")
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

}