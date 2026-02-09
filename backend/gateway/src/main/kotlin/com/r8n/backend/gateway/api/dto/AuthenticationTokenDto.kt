package com.r8n.backend.gateway.api.dto

class AuthenticationTokenDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)