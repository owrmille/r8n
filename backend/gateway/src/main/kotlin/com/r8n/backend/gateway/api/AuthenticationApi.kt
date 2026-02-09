package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.AuthenticationTokenDto

interface AuthenticationApi {
    fun login(email: String, password: String): AuthenticationTokenDto
    fun logout()
    fun refresh(refreshToken: String): AuthenticationTokenDto
}