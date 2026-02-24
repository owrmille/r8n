package com.r8n.backend.gateway.api.dto

data class AuthenticationTokenDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresInMilliseconds: Long,
)