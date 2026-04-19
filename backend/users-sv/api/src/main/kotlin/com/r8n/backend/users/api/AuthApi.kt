package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.AuthenticationTokenDto
import com.r8n.backend.users.api.dto.LoginRequestDto
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface AuthApi {
    companion object {
        const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
        private const val ROOT_PATH = "/api/auth"
        const val LOGIN_PATH = "$ROOT_PATH/login"
        const val LOGOUT_PATH = "$ROOT_PATH/logout"
        const val REFRESH_PATH = "$ROOT_PATH/refresh"
    }

    @PostMapping(LOGIN_PATH)
    fun login(
        @RequestBody request: LoginRequestDto,
    ): AuthenticationTokenDto

    @PostMapping(LOGOUT_PATH)
    fun logout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) refreshToken: String?,
    )

    @PostMapping(REFRESH_PATH)
    fun refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) refreshToken: String?,
    ): AuthenticationTokenDto
}