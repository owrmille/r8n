package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.AuthenticationTokenDto
import com.r8n.backend.users.api.dto.LoginRequestDto
import com.r8n.backend.users.api.dto.RegisterRequestDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

interface AuthApi {
    companion object {
        const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
        private const val ROOT_PATH = "/api/auth"
        const val CSRF_PATH = "$ROOT_PATH/csrf"
        const val LOGIN_PATH = "$ROOT_PATH/login"
        const val REGISTER_PATH = "$ROOT_PATH/register"
        const val LOGOUT_PATH = "$ROOT_PATH/logout"
        const val REFRESH_PATH = "$ROOT_PATH/refresh"
    }

    @GetMapping(CSRF_PATH)
    fun csrf()

    @PostMapping(LOGIN_PATH)
    fun login(
        @RequestBody request: LoginRequestDto,
    ): AuthenticationTokenDto

    @PostMapping(REGISTER_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @RequestBody request: RegisterRequestDto,
    )

    @PostMapping(LOGOUT_PATH)
    fun logout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) refreshToken: String?,
    )

    @PostMapping(REFRESH_PATH)
    fun refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) refreshToken: String?,
    ): AuthenticationTokenDto
}
