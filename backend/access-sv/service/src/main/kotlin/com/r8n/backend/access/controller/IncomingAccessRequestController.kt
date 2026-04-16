package com.r8n.backend.access.controller

import com.r8n.backend.access.api.IncomingAccessRequestApi
import com.r8n.backend.access.api.dto.access.AccessRequestDto
import com.r8n.backend.access.api.dto.access.RequestStatusEnumDto
import com.r8n.backend.access.facade.AccessRequestFacade
import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.security.Authority.IS_USER
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
class IncomingAccessRequestController(
    private val facade: AccessRequestFacade
) : IncomingAccessRequestApi {

    @PreAuthorize(IS_USER)
    override fun get(
        forListId: UUID?,
        since: Instant?,
        status: RequestStatusEnumDto?,
        pageable: PageRequestDto,
    ): PageResponseDto<AccessRequestDto> {
        return facade.getIncoming(forListId, since, status, pageable, getCurrentUserId())
    }

    @PreAuthorize(IS_USER)
    override fun accept(requestId: UUID): AccessRequestDto {
        return facade.acceptRequest(requestId, getCurrentUserId())
    }

    @PreAuthorize(IS_USER)
    override fun decline(requestId: UUID): AccessRequestDto {
        return facade.declineRequest(requestId, getCurrentUserId())
    }

    @PreAuthorize(IS_USER)
    override fun hide(requestId: UUID): AccessRequestDto {
        return facade.hideRequest(requestId, getCurrentUserId())
    }
}
