package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.access.AccessRequestDto
import com.r8n.backend.gateway.api.dto.access.RequestStatusEnumDto
import java.time.Instant
import java.util.UUID

interface IncomingAccessRequestsApi {
    fun getAll(since: Instant?, status: RequestStatusEnumDto?): List<AccessRequestDto>
    fun getForList(listId: UUID, since: Instant?, status: RequestStatusEnumDto?): List<AccessRequestDto>
    fun accept(requestId: UUID): AccessRequestDto
    fun decline(requestId: UUID): AccessRequestDto
    fun hide(requestId: UUID): AccessRequestDto
}