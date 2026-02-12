package com.r8n.backend.gateway.stub

import com.r8n.backend.gateway.api.dto.access.AccessRequestDto
import com.r8n.backend.gateway.api.dto.access.RequestStatusEnumDto
import java.time.Instant
import java.util.UUID

object AccessRequestsTestDataFactory {
    fun get(listId: UUID? = null, status: RequestStatusEnumDto = RequestStatusEnumDto.SENT): AccessRequestDto {
        val listId = listId ?: UUID.randomUUID()
        return AccessRequestDto(
            UUID.randomUUID(),
            listId,
            "the most complete rating of ${listId}s",
            UUID.randomUUID(),
            "world's leading expert in ${listId}s",
            UUID.randomUUID(),
            "world's biggest fan of ${listId}s",
            Instant.now(),
            status,
        )
    }
}