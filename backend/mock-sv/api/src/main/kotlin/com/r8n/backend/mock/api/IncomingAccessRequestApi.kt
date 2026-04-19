package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.access.AccessRequestDto
import com.r8n.backend.mock.api.dto.access.RequestStatusEnumDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant
import java.util.UUID

interface IncomingAccessRequestApi {
    companion object {
        private const val ROOT_PATH = "/api/access-requests/incoming"
        const val GET_PATH = ROOT_PATH
        const val ACCEPT_PATH = "$ROOT_PATH/{requestId}/accept"
        const val DECLINE_PATH = "$ROOT_PATH/{requestId}/decline"
        const val HIDE_PATH = "$ROOT_PATH/{requestId}/hide"
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

    @PostMapping(ACCEPT_PATH)
    fun accept(
        @PathVariable requestId: UUID,
    ): AccessRequestDto

    @PostMapping(DECLINE_PATH)
    fun decline(
        @PathVariable requestId: UUID,
    ): AccessRequestDto

    @PostMapping(HIDE_PATH)
    fun hide(
        @PathVariable requestId: UUID,
    ): AccessRequestDto
}