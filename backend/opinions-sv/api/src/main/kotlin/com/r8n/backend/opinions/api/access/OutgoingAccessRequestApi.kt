package com.r8n.backend.opinions.api.access

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.access.dto.AccessRequestDto
import com.r8n.backend.opinions.api.access.dto.RequestStatusEnumDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant
import java.util.UUID

interface OutgoingAccessRequestApi {
    companion object {
        private const val ROOT_PATH = "/api/access-requests/outgoing"
        const val GET_PATH = ROOT_PATH
        const val CREATE_PATH = "$ROOT_PATH/create/{listId}"
        const val CANCEL_PATH = "$ROOT_PATH/cancel/{requestId}"
    }

    @GetMapping(GET_PATH)
    fun get(
        @RequestParam(required = false)
        forListId: UUID?,
        @RequestParam(required = false)
        since: Instant?,
        @RequestParam(required = false)
        status: RequestStatusEnumDto?,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<AccessRequestDto>

    @PostMapping(CREATE_PATH)
    fun create(
        @PathVariable listId: UUID,
    ): AccessRequestDto

    @PostMapping(CANCEL_PATH)
    fun cancel(
        @PathVariable requestId: UUID,
    ): AccessRequestDto
}
