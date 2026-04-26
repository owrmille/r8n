package com.r8n.backend.users.service

import com.r8n.backend.users.domain.User
import com.r8n.backend.users.domain.UserProfile
import com.r8n.backend.users.domain.Username
import com.r8n.backend.users.persistence.RoleEnumPersistence
import com.r8n.backend.users.provider.database.PIIRepository
import com.r8n.backend.users.provider.database.UserRepository
import com.r8n.backend.users.provider.database.UserRoleAssignmentRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class UserService(
    private val consentService: ConsentService,
    private val userRepository: UserRepository,
    private val piiRepository: PIIRepository,
    private val userRoleAssignmentRepository: UserRoleAssignmentRepository,
) {
    fun getName(id: UUID) = piiRepository.findByIdOrNull(id)?.name ?: "Unknown"

    fun isAnyModerator(id: UUID): Boolean = isHumanModerator(id) || isAiModerator(id)

    fun isHumanModerator(id: UUID): Boolean = hasRole(id, RoleEnumPersistence.MODERATOR)

    fun isAiModerator(id: UUID): Boolean = hasRole(id, RoleEnumPersistence.AI_MODERATOR)

    fun isAdmin(id: UUID): Boolean = hasRole(id, RoleEnumPersistence.ADMIN)

    private fun hasRole(
        userId: UUID,
        role: RoleEnumPersistence,
    ): Boolean = userRoleAssignmentRepository.findAllByUser(userId).any { it.role == role }

    fun getUser(id: UUID): User {
        val userPersistence =
            userRepository.findByIdOrNull(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $id not found")
        val piiPersistence =
            piiRepository.findByIdOrNull(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "PII for user $id not found")
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

    fun getMyName(userId: UUID): Username = Username(userId, getName(userId))

    fun getProfile(id: UUID): UserProfile {
        val user =
            userRepository.findByIdOrNull(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $id not found")
        val pii =
            piiRepository.findByIdOrNull(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "PII for user $id not found")
        return UserProfile(
            id,
            pii.name,
            user.status,
            user.lastSeenAt,
            pii.about,
            pii.location,
        )
    }
}