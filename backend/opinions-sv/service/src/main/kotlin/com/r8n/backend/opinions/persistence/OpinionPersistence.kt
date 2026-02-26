package com.r8n.backend.opinions.persistence

import com.r8n.backend.opinions.domain.OpinionStatusEnum
import jakarta.persistence.Entity
import java.time.Instant
import java.util.UUID

@Entity
class OpinionPersistence(
    val id: UUID,
    val owner: UUID,
    val subject: UUID,
    val mark: Double?,
    val status: OpinionStatusEnum,
    val timestamp: Instant,
)
