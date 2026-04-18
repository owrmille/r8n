package com.r8n.backend.users.service

import com.r8n.backend.users.api.dto.LoginRequestDto
import com.r8n.backend.users.provider.database.UserRepository
import com.r8n.backend.users.provider.database.UserRoleAssignmentRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userRoleAssignmentRepository: UserRoleAssignmentRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
) {
    fun login(request: LoginRequestDto): AuthenticationTokens {
        val user =
            userRepository.findByEmail(request.login)
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        if (user.passwordHash == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }

        val roles = userRoleAssignmentRepository.findAllByUser(user.id).map { it.role.name }
        val accessToken = tokenService.generateAccessToken(user.id, roles.ifEmpty { listOf("USER") })
        val refreshToken = tokenService.generateRefreshToken(user.id)

        return AuthenticationTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInMilliseconds = tokenService.getAccessTokenExpirationMillis(),
        )
    }

    fun logout(refreshToken: String?) {
        if (refreshToken != null) {
            try {
                // When logging out, we want to revoke the whole token family
                // because this session is explicitly ended.
                val (userId, _) = tokenService.validateAndRotateRefreshToken(refreshToken)
                tokenService.revokeAllTokensForUser(userId)
            } catch (_: Exception) {
                // Ignore errors during logout
            }
        }
    }

    fun logoutAll(userId: UUID) {
        tokenService.revokeAllTokensForUser(userId)
    }

    fun refresh(refreshToken: String?): AuthenticationTokens {
        val (userId, newRefreshToken) =
            tokenService.validateAndRotateRefreshToken(
                refreshToken ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token"),
            )

        val roles = userRoleAssignmentRepository.findAllByUser(userId).map { it.role.name }
        val accessToken = tokenService.generateAccessToken(userId, roles.ifEmpty { listOf("USER") })

        return AuthenticationTokens(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            expiresInMilliseconds = tokenService.getAccessTokenExpirationMillis(),
        )
    }
}