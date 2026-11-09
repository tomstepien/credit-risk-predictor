package com.creditrisk.exception

import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime


@ControllerAdvice
class ExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun body(status: HttpStatus, error: String, message: Any) = ResponseEntity(
        mapOf(
            "timestamp" to LocalDateTime.now(),
            "status" to status.value(),
            "error" to error,
            "message" to message
        ),
        status
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity(
            mapOf(
                "timestamp" to LocalDateTime.now(),
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Validation Failed",
                "message" to "Request contains invalid fields",
                "details" to errors
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(e: HttpMessageNotReadableException) =
        body(HttpStatus.BAD_REQUEST, "Malformed Request", "Request body is missing or not valid JSON for this endpoint")

    @ExceptionHandler(NotFoundException::class, EntityNotFoundException::class)
    fun handleNotFound(e: RuntimeException) =
        body(HttpStatus.NOT_FOUND, "Not Found", e.message ?: "Resource not found")

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(e: ConflictException) =
        body(HttpStatus.CONFLICT, "Conflict", e.message ?: "Conflict")

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(e: ResponseStatusException): ResponseEntity<Map<String, Any>> {
        val status = HttpStatus.valueOf(e.statusCode.value())
        return body(status, status.reasonPhrase, e.reason ?: status.reasonPhrase)
    }

    @ExceptionHandler(WebClientResponseException::class)
    fun handleMlResponseError(e: WebClientResponseException): ResponseEntity<Map<String, Any>> {
        log.warn("ML service returned {}: {}", e.statusCode, e.responseBodyAsString)
        return if (e.statusCode.is4xxClientError) {
            body(HttpStatus.UNPROCESSABLE_ENTITY, "ML Model Rejected Input", e.responseBodyAsString.ifBlank { e.message })
        } else {
            body(HttpStatus.BAD_GATEWAY, "ML Model Service Error", "ML service failed with status ${e.statusCode.value()}")
        }
    }

    @ExceptionHandler(WebClientRequestException::class)
    fun handleMlUnreachable(e: WebClientRequestException): ResponseEntity<Map<String, Any>> {
        log.warn("ML service unreachable: {}", e.message)
        return body(HttpStatus.SERVICE_UNAVAILABLE, "ML Model Service Unavailable", "ML service is currently unreachable")
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(e: Exception): ResponseEntity<Map<String, Any>> {
        log.error("Unhandled exception", e)
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", e.message ?: "An unexpected error occurred")
    }
}
