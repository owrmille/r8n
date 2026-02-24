package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.AuthenticationTokenDto
import com.r8n.backend.gateway.api.dto.LoginRequestDto
import org.springframework.http.ResponseEntity

interface AuthenticationApi {
    fun login(request: LoginRequestDto): ResponseEntity<AuthenticationTokenDto>
    fun logout()
    fun refresh(refreshToken: String): ResponseEntity<AuthenticationTokenDto>
}