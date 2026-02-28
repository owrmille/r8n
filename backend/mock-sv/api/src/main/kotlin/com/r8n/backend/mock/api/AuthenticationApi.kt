package com.r8n.backend.mock.api

import com.r8n.backend.mock.api.dto.AuthenticationTokenDto
import com.r8n.backend.mock.api.dto.LoginRequestDto

interface AuthenticationApi {
    fun login(request: LoginRequestDto): AuthenticationTokenDto
    fun logout()
    fun refresh(refreshToken: String): AuthenticationTokenDto
}