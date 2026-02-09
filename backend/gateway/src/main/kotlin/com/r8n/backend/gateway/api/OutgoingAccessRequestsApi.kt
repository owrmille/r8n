package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.access.AccessRequestDto
import com.r8n.backend.gateway.api.dto.access.RequestStatusEnumDto
import java.time.Instant
import java.util.UUID

interface OutgoingAccessRequestsApi {
    fun getAll(since: Instant?, status: RequestStatusEnumDto?): List<AccessRequestDto>
    fun getForList(listId: UUID, since: Instant?, status: RequestStatusEnumDto?): List<AccessRequestDto>
    fun create(id: UUID): AccessRequestDto
    fun cancel(id: UUID): AccessRequestDto
}
