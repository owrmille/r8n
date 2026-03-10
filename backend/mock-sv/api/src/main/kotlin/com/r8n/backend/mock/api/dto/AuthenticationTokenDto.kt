package com.r8n.backend.mock.api.dto

data class AuthenticationTokenDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresInMilliseconds: Long,
)