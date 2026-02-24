package com.r8n.backend.gateway.api.dto

data class LoginRequestDto(
    val login: String,
    val password: String,
)