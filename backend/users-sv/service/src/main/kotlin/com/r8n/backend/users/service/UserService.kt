package com.r8n.backend.users.service

import com.r8n.backend.users.domain.User
import com.r8n.backend.users.provider.database.PIIRepository
import com.r8n.backend.users.provider.database.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val consentService: ConsentService,
    private val userRepository: UserRepository,
    private val piiRepository: PIIRepository,
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
            status = userPersistence.status,
            statusTimestamp = userPersistence.statusTimestamp,
            consents = consents,
        )
    }
}