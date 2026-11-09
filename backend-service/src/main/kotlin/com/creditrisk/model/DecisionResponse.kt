package com.creditrisk.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class DecisionResponse(
    @field:JsonProperty("credit_application_id")
    val creditApplicationId: String,
    val probability: Double,

    @field:JsonProperty("is_risky")
    val isRisky: Boolean,

    val decision: String,
    @field:JsonAlias("model_version")
    val modelVersion: String = "unknown",

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime? = null,
)