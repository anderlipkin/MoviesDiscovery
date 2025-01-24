package com.example.moviesdiscovery.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    @SerialName("status_message")
    val message: String,
    @SerialName("status_code")
    val code: Int,
    @SerialName("success")
    val success: Boolean
)
