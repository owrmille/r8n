package com.r8n.backend.users.service

import com.r8n.backend.users.domain.UserSession
import com.r8n.backend.users.provider.database.UserSessionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserSessionService(
    private val userSessionRepository: UserSessionRepository,
) {
    fun getSessionsForUser(userId: UUID, pageable: Pageable): Page<UserSession> {
        return userSessionRepository.findAllByUserId(userId, pageable).map {
            it.toDomain()
        }
    }

    fun getSession(id: UUID, userId: UUID): UserSession {
        return userSessionRepository.findByIdAndUserId(id, userId)?.toDomain()
            ?: throw NoSuchElementException("Session $id not found for user $userId")
    }

    private fun com.r8n.backend.users.persistence.UserSessionPersistence.toDomain() = UserSession(
        id = id,
        created = created,
        expires = expires,
        ip = ip,
        userAgent = userAgent
    )
}
