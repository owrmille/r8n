package com.r8n.backend.users.service

import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import com.r8n.backend.users.domain.User
import com.r8n.backend.users.domain.UserProfile
import com.r8n.backend.users.domain.Username
import com.r8n.backend.users.provider.database.PIIRepository
import com.r8n.backend.users.provider.database.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val consentService: ConsentService,
    private val userRepository: UserRepository,
    private val piiRepository: PIIRepository,
    private val userSessionService: UserSessionService,
) {
    fun getName(id: UUID) = piiRepository.findByIdOrNull(id)?.name ?: "Unknown"

    fun getUser(id: UUID): User {
        val userPersistence =
            userRepository.findByIdOrNull(id)
                ?: throw NoSuchElementException("User $id not found")
        val piiPersistence =
            piiRepository.findByIdOrNull(id)
                ?: throw NoSuchElementException("PII for user $id not found")
        val consents = consentService.getConsentsForUser(id)

        return User(
            id = userPersistence.id,
            name = piiPersistence.name,
            email = piiPersistence.email,
            phone = piiPersistence.phone,
            status = userPersistence.status,
            about = piiPersistence.about,
            location = piiPersistence.location,
            statusTimestamp = userPersistence.statusTimestamp,
            consents = consents,
        )
    }

    fun getMyName(): Username {
        val id = getCurrentUserId()
        return Username(id, getName(id))
    }

    fun lastOnline(id: UUID): Instant? {
        val lastSession = userSessionService.lastSessionForUserId(id)
        return lastSession?.expires?.let { maxOf(Instant.now(), it) }
    }

    fun getProfile(id: UUID): UserProfile {
        val user =
            userRepository.findByIdOrNull(id)
                ?: throw NoSuchElementException("User $id not found")
        val pii =
            piiRepository.findByIdOrNull(id)
                ?: throw NoSuchElementException("PII for user $id not found")
        return UserProfile(
            id,
            getName(id),
            user.status,
            lastOnline(id),
            pii.about,
            pii.location,
        )
    }
}