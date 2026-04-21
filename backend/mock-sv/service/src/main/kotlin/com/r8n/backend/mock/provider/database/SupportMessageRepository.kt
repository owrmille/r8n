package com.r8n.backend.mock.provider.database

import com.r8n.backend.mock.persistence.SupportMessagePersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SupportMessageRepository : JpaRepository<SupportMessagePersistence, UUID> {
    fun findAllByThreadIdOrderByCreatedAtAsc(threadId: UUID): List<SupportMessagePersistence>
}