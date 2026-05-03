package com.creditrisk.model

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
    val modelVersion: String = "xgb_v1",

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime? = null,
)