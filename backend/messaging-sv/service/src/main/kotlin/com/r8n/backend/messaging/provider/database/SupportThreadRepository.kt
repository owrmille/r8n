package com.r8n.backend.messaging.provider.database

import com.r8n.backend.messaging.persistence.SupportThreadPersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SupportThreadRepository : JpaRepository<SupportThreadPersistence, UUID> {
    fun findAllByOrderByUpdatedAtDesc(pageable: Pageable): Page<SupportThreadPersistence>

    fun findAllByOwnerUserIdOrderByUpdatedAtDesc(
        ownerUserId: UUID,
        pageable: Pageable,
    ): Page<SupportThreadPersistence>
}