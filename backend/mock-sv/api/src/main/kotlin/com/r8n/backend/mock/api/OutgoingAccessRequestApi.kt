package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.access.AccessRequestDto
import com.r8n.backend.mock.api.dto.access.RequestStatusEnumDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant
import java.util.UUID

interface OutgoingAccessRequestApi {
    companion object {
        const val GET_PATH = "/access-requests/outgoing"
        const val CREATE_PATH = "/access-requests/outgoing/create/{listId}"
        const val CANCEL_PATH = "/access-requests/outgoing/cancel/{requestId}"
    }

    @GetMapping(GET_PATH)
    fun get(
        @RequestParam(required = false)
        forListId: UUID?,
        @RequestParam(required = false)
        since: Instant?,
        @RequestParam(required = false)
        status: RequestStatusEnumDto?,
        pageable: PageRequestDto,
    ): PageResponseDto<AccessRequestDto>

    @GetMapping(CREATE_PATH)
    fun create(
        @PathVariable listId: UUID,
    ): AccessRequestDto

    @GetMapping(CANCEL_PATH)
    fun cancel(
        @PathVariable requestId: UUID,
    ): AccessRequestDto
}