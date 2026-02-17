package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.PageResponse
import com.r8n.backend.gateway.api.dto.access.AccessRequestDto
import com.r8n.backend.gateway.api.dto.access.RequestStatusEnumDto
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

interface IncomingAccessRequestsApi {
    fun get(
        forListId: UUID?,
        since: Instant?,
        status: RequestStatusEnumDto?,
        pageable: Pageable,
    ): PageResponse<AccessRequestDto>

    fun accept(requestId: UUID): AccessRequestDto
    fun decline(requestId: UUID): AccessRequestDto
    fun hide(requestId: UUID): AccessRequestDto
}