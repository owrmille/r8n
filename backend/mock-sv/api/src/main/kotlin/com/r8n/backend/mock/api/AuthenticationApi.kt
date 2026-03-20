package com.r8n.backend.mock.api

import com.r8n.backend.mock.api.dto.AuthenticationTokenDto
import com.r8n.backend.mock.api.dto.LoginRequestDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

interface AuthenticationApi {
    companion object {
        const val LOGIN_PATH = "/auth/login"
        const val LOGOUT_PATH = "/auth/logout"
        const val REFRESH_PATH = "/auth/refresh"
    }

    @PostMapping(LOGIN_PATH)
    fun login(@RequestBody request: LoginRequestDto): AuthenticationTokenDto

    @PostMapping(LOGOUT_PATH)
    fun logout()

    @PostMapping(REFRESH_PATH)
    fun refresh(
        @RequestParam(required = true)
        refreshToken: String,
    ): AuthenticationTokenDto
}