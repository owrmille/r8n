package com.r8n.backend.gateway.controller

import com.r8n.backend.gateway.api.AuthenticationApi
import com.r8n.backend.gateway.api.dto.AuthenticationTokenDto
import com.r8n.backend.gateway.api.dto.LoginRequestDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class StubAuthenticationController : AuthenticationApi {

    companion object {
        const val STUB_LOGIN = "test"
        const val STUB_PASSWORD = "1234"

        const val STUB_ACCESS_TOKEN = "stub-access-token-123"
        const val STUB_REFRESH_TOKEN = "stub-refresh-token-456"
    }

    @PostMapping("/login")
    override fun login(@RequestBody request: LoginRequestDto): ResponseEntity<AuthenticationTokenDto> {

        if (request.login == STUB_LOGIN &&
            request.password == STUB_PASSWORD
        ) {
            return ResponseEntity.ok(
                AuthenticationTokenDto(
                    STUB_ACCESS_TOKEN,
                    STUB_REFRESH_TOKEN,
                    0,
                ),
            )
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    @PostMapping("/logout")
    override fun logout() = Unit

    @PostMapping("/refresh")
    override fun refresh(refreshToken: String) = login(LoginRequestDto(STUB_ACCESS_TOKEN, refreshToken))
}