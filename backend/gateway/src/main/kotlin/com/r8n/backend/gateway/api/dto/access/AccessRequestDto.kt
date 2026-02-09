package com.r8n.backend.gateway.api.dto.access

import com.r8n.backend.gateway.api.dto.access.RequestStatusEnumDto
import java.time.Instant
import java.util.UUID

class AccessRequestDto(
    val id: UUID,
    val opinionListId: UUID,
    val opinionListName: String,
    val owner: UUID,
    val ownerName: String,
    val requester: UUID,
    val requesterName: String,
    val timestamp: Instant,
    val status: RequestStatusEnumDto,
)