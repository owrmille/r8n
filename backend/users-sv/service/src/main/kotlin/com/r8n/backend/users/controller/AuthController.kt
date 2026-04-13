package com.r8n.backend.users.controller

import com.r8n.backend.users.api.AuthApi.Companion.REFRESH_TOKEN_COOKIE_NAME
import com.r8n.backend.users.api.AuthApi
import com.r8n.backend.users.api.dto.AuthenticationTokenDto
import com.r8n.backend.users.api.dto.LoginRequestDto
import com.r8n.backend.users.service.AuthService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Duration

@RestController
class AuthController(
    private val authService: AuthService,
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
        val tokens =
            authService.refresh(
                refreshToken
                    ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token"),
            )
        addRefreshTokenCookie(tokens.refreshToken)
        return AuthenticationTokenDto(
            accessToken = tokens.accessToken,
            expiresInMilliseconds = tokens.expiresInMilliseconds,
        )
    }

    private fun addRefreshTokenCookie(refreshToken: String) {
        currentResponse().addHeader(
            HttpHeaders.SET_COOKIE,
            ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .path("/auth")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(30))
                .build()
                .toString(),
        )
    }

    private fun clearRefreshTokenCookie() {
        currentResponse().addHeader(
            HttpHeaders.SET_COOKIE,
            ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .path("/auth")
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build()
                .toString(),
        )
    }

    private fun currentResponse() =
        (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).response
            ?: throw IllegalStateException("No current HTTP response")
}
