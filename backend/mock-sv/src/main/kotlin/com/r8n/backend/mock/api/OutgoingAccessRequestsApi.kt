package com.r8n.backend.mock.api

import com.r8n.backend.mock.api.dto.PageResponse
import com.r8n.backend.mock.api.dto.access.AccessRequestDto
import com.r8n.backend.mock.api.dto.access.RequestStatusEnumDto
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

interface OutgoingAccessRequestsApi {
    fun get(
        forListId: UUID?,
        since: Instant?,
        status: RequestStatusEnumDto?,
        pageable: Pageable,
    ): PageResponse<AccessRequestDto>

    fun create(listId: UUID): AccessRequestDto
    fun cancel(requestId: UUID): AccessRequestDto
}
