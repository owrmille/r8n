package com.r8n.backend.opinionlists.provider.database

import com.r8n.backend.opinionlists.persistence.OutboxEvent
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OutboxRepository : JpaRepository<OutboxEvent, UUID> {
    fun findTop100ByPublishedAtIsNullOrderByCreatedAtAsc(): List<OutboxEvent>
}