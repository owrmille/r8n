package com.r8n.backend.mock.controller

import com.r8n.backend.mock.api.AuthenticationApi
import com.r8n.backend.mock.api.dto.AuthenticationTokenDto
import com.r8n.backend.mock.api.dto.LoginRequestDto
import com.r8n.backend.security.SecurityAutoConfiguration.Companion.STUB_ACCESS_TOKEN
import com.r8n.backend.security.SecurityAutoConfiguration.Companion.STUB_LOGIN
import com.r8n.backend.security.SecurityAutoConfiguration.Companion.STUB_PASSWORD
import com.r8n.backend.security.SecurityAutoConfiguration.Companion.STUB_REFRESH_TOKEN
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class StubAuthenticationController : AuthenticationApi {

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