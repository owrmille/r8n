package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.access.AccessRequestDto
import com.r8n.backend.mock.api.dto.access.RequestStatusEnumDto
import java.time.Instant
import java.util.UUID

interface IncomingAccessRequestsApi {
    fun get(
        forListId: UUID?,
        since: Instant?,
        status: RequestStatusEnumDto?,
        pageable: PageRequestDto,
    ): PageResponseDto<AccessRequestDto>

    fun accept(requestId: UUID): AccessRequestDto
    fun decline(requestId: UUID): AccessRequestDto
    fun hide(requestId: UUID): AccessRequestDto
}