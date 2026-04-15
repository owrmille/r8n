package com.r8n.backend.users.controller

import com.r8n.backend.users.api.AuthApi
import com.r8n.backend.users.api.dto.AuthenticationTokenDto
import com.r8n.backend.users.api.dto.LoginRequestDto
import com.r8n.backend.users.security.RefreshTokenCookieFactory
import com.r8n.backend.users.service.AuthService
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RestController
class AuthController(
    private val authService: AuthService,
    private val refreshTokenCookieFactory: RefreshTokenCookieFactory,
) : AuthApi {
    override fun login(request: LoginRequestDto): AuthenticationTokenDto {
        val tokens = authService.login(request)
        addRefreshTokenCookie(tokens.refreshToken)
        return AuthenticationTokenDto(
            accessToken = tokens.accessToken,
            expiresInMilliseconds = tokens.expiresInMilliseconds,
        )
    }

    override fun logout() {
        authService.logout()
        clearRefreshTokenCookie()
    }

    override fun refresh(refreshToken: String?): AuthenticationTokenDto {
        val tokens = authService.refresh(refreshToken)
        addRefreshTokenCookie(tokens.refreshToken)
        return AuthenticationTokenDto(
            accessToken = tokens.accessToken,
            expiresInMilliseconds = tokens.expiresInMilliseconds,
        )
    }

    private fun addRefreshTokenCookie(refreshToken: String) {
        currentResponse().addHeader(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieFactory.create(refreshToken).toString(),
        )
    }

    private fun clearRefreshTokenCookie() {
        currentResponse().addHeader(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieFactory.clear().toString(),
        )
    }

    private fun currentResponse() =
        (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).response
            ?: throw IllegalStateException("No current HTTP response")
}
