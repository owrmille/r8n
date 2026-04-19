package com.r8n.backend.access.domain

import java.time.Instant
import java.util.UUID

class AccessRequest(
    var id: UUID? = null,
    var listId: UUID,
    var requesterId: UUID,
    var ownerId: UUID,
    var status: RequestStatusEnum,
    var createdAt: Instant,
    var updatedAt: Instant,
)