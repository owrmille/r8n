package com.r8n.backend.users.service

import com.r8n.backend.users.persistence.ApiKeyPersistence
import com.r8n.backend.users.provider.database.ApiKeyRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class ApiKeyService(
    private val apiKeyRepository: ApiKeyRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    private val secureRandom = SecureRandom()

    @Transactional
    fun createApiKey(userId: UUID, name: String, expiresAt: Instant? = null): String {
        val rawKey = generateSecureKey()
        val hashedKey = passwordEncoder.encode(rawKey)

        val apiKey = ApiKeyPersistence(
            userId = userId,
            keyHash = hashedKey!!,
            name = name,
            createdAt = Instant.now(),
            expiresAt = expiresAt
        )
        apiKeyRepository.save(apiKey)

        // Return prefix + raw key for the user to store
        return "r8n_${Base64.getEncoder().withoutPadding().encodeToString(rawKey.toByteArray())}"
    }

    @Transactional
    fun validateApiKey(rawKeyWithPrefix: String): UUID {
        if (!rawKeyWithPrefix.startsWith("r8n_")) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key format")
        }

        val rawKey = String(Base64.getDecoder().decode(rawKeyWithPrefix.substring(4)))
        
        // This is inefficient as we can't look up by hash directly since BCrypt is salted.
        // For a real production system with many keys, we'd use a key prefix/id or a faster hash.
        // Given the "Smallest Change" and "Maintainability" principles, we'll implement a simple lookup.
        // Optimization: In a real system, we might store a non-salted hash (e.g., SHA-256) of the key for lookup,
        // and then verify with BCrypt for security.
        
        // For now, let's find all active keys and verify. 
        // Better: Use a prefix of the key (e.g. first 8 chars) stored in plain text to narrow down the search.
        
        // Let's refine the implementation to be more performant while keeping it simple.
        val allKeys = apiKeyRepository.findAll().filter { !it.revoked && (it.expiresAt == null || it.expiresAt.isAfter(Instant.now())) }
        
        val matchedKey = allKeys.find { passwordEncoder.matches(rawKey, it.keyHash) }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key")

        matchedKey.lastUsedAt = Instant.now()
        apiKeyRepository.save(matchedKey)

        return matchedKey.userId
    }

    private fun generateSecureKey(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getEncoder().withoutPadding().encodeToString(bytes)
    }

    @Transactional
    fun revokeApiKey(userId: UUID, keyId: UUID) {
        val apiKey = apiKeyRepository.findById(keyId)
            .filter { it.userId == userId }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found") }
        
        apiKey.revoked = true
        apiKeyRepository.save(apiKey)
    }
}
