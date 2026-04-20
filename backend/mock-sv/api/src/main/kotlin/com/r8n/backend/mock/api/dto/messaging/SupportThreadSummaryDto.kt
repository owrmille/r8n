package com.r8n.backend.mock.api.dto.messaging

import java.time.Instant
import java.util.UUID

data class SupportThreadSummaryDto(
    val id: UUID,
    val ownerUserId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastMessageAt: Instant?,
)