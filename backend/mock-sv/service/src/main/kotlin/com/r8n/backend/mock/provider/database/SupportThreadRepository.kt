package com.r8n.backend.mock.provider.database

import com.r8n.backend.mock.persistence.SupportThreadPersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SupportThreadRepository : JpaRepository<SupportThreadPersistence, UUID> {
    fun findAllByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId: UUID): List<SupportThreadPersistence>
}