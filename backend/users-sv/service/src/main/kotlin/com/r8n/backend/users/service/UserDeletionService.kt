package com.r8n.backend.users.service

import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.users.provider.database.PIIRepository
import com.r8n.backend.users.provider.database.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserDeletionService(
    private val userRepository: UserRepository,
    private val piiRepository: PIIRepository,
    private val opinionListsClient: OpinionListsInternalApi,
    private val messagingClient: MessagingApi,
) {
    private val logger = LoggerFactory.getLogger(UserDeletionService::class.java)

    @Transactional
    fun deleteUser(userId: UUID) {
        logger.info("Starting deletion for user: {}", userId)

        try {
            // Delete data from other services first
            logger.debug("Deleting opinions data for user: {}", userId)
            opinionListsClient.deleteAllUserDataForUser(userId)

            logger.debug("Deleting messaging data for user: {}", userId)
            messagingClient.deleteAllUserDataForUser(userId)

            // Delete user - cascading will handle PII, sessions, consents, role assignments, refresh tokens, profile avatars
            logger.debug("Deleting user record: {}", userId)
            userRepository.deleteById(userId)

            logger.info("Successfully deleted user: {}", userId)
        } catch (e: Exception) {
            logger.error("Failed to delete user: {}", userId, e)
            throw e
        }
    }

    fun validateEmailConfirmation(
        userId: UUID,
        providedEmail: String,
    ): Boolean {
        val pii = piiRepository.findById(userId)
        return pii.isPresent && pii.get().email.equals(providedEmail, ignoreCase = true)
    }
}