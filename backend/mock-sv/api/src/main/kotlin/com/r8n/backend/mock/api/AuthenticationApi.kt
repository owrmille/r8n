package com.r8n.backend.mock.api

import com.r8n.backend.mock.api.dto.AuthenticationTokenDto
import com.r8n.backend.mock.api.dto.LoginRequestDto
import org.springframework.http.ResponseEntity

interface AuthenticationApi {
    fun login(request: LoginRequestDto): ResponseEntity<AuthenticationTokenDto>
    fun logout()
    fun refresh(refreshToken: String): ResponseEntity<AuthenticationTokenDto>
}