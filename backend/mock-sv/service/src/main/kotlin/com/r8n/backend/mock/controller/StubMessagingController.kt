package com.r8n.backend.mock.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.dto.messaging.CreateSupportMessageRequestDto
import com.r8n.backend.mock.api.dto.messaging.CreateSupportThreadRequestDto
import com.r8n.backend.mock.api.dto.messaging.SupportParticipantRoleEnumDto
import com.r8n.backend.mock.api.dto.messaging.SupportThreadSummaryDto
import com.r8n.backend.mock.service.SupportActor
import com.r8n.backend.mock.service.SupportMessagingService
import com.r8n.backend.security.Authority
import com.r8n.backend.security.Authority.IS_USER_OR_SUPPORT
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class StubMessagingController(
    private val supportMessagingService: SupportMessagingService,
) : MessagingApi {
    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun getSupportThreads() =
        PageImpl(supportMessagingService.getSupportThreads(getAuthenticatedUserContext())).toResponse()

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun getSupportThreadSummaries(pageable: PageRequestDto) =
        PageImpl(supportMessagingService.listVisibleThreadSummaries(getAuthenticatedUserContext())).toResponse()

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun createSupportThread(request: CreateSupportThreadRequestDto): SupportThreadSummaryDto =
        supportMessagingService.createSupportThread(getAuthenticatedUserContext(), request)

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun getSupportThreadMessages(
        threadId: UUID,
        pageable: PageRequestDto,
    ) = PageImpl(supportMessagingService.getSupportThreadMessages(getAuthenticatedUserContext(), threadId)).toResponse()

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun addSupportThreadMessage(
        threadId: UUID,
        request: CreateSupportMessageRequestDto,
    ) = supportMessagingService.addSupportThreadMessage(getAuthenticatedUserContext(), threadId, request)

    private fun getAuthenticatedUserContext(): SupportActor {
        val authentication =
            SecurityContextHolder.getContext().authentication
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication")
        val userId =
            runCatching { UUID.fromString(authentication.name) }
                .getOrElse {
                    throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user id")
                }
        val isSupport = authentication.authorities.any { it.authority == Authority.SUPPORT }
        val role = if (isSupport) SupportParticipantRoleEnumDto.SUPPORT else SupportParticipantRoleEnumDto.USER
        return SupportActor(userId = userId, isSupport = isSupport, role = role)
    }
}