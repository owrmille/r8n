package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.AuthenticationTokenDto
import com.r8n.backend.users.api.dto.LoginRequestDto
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface AuthApi {
    companion object {
        const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
    }

    @PostMapping("/auth/login")
    fun login(
        @RequestBody request: LoginRequestDto,
    ): AuthenticationTokenDto

    @PostMapping("/auth/logout")
    fun logout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) refreshToken: String?,
    )

    @PostMapping("/auth/refresh")
    fun refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) refreshToken: String?,
    ): AuthenticationTokenDto
}