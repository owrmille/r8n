package com.r8n.backend.opinions.persistence

import com.r8n.backend.opinions.domain.OpinionStatusEnum
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.Instant
import java.util.UUID

@Entity
class OpinionPersistence(
    @Id
    @GeneratedValue
    val id: UUID,
    val owner: UUID,
    val subject: UUID,
    val mark: Double?,
    val status: OpinionStatusEnum,
    val timestamp: Instant,
)
