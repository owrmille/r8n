package com.r8n.backend.messaging.facade

import com.r8n.backend.messaging.api.dto.SupportThreadDto
import com.r8n.backend.messaging.api.dto.messaging.CreateSupportMessageRequestDto
import com.r8n.backend.messaging.api.dto.messaging.CreateSupportThreadRequestDto
import com.r8n.backend.messaging.api.dto.messaging.SupportMessageDto
import com.r8n.backend.messaging.api.dto.messaging.SupportParticipantRoleEnumDto
import com.r8n.backend.messaging.api.dto.messaging.SupportThreadSummaryDto
import com.r8n.backend.messaging.persistence.SupportMessagePersistence
import com.r8n.backend.messaging.persistence.SupportParticipantRoleEnumPersistence
import com.r8n.backend.messaging.persistence.SupportThreadPersistence
import com.r8n.backend.messaging.service.SupportActor
import com.r8n.backend.messaging.service.SupportMessagingService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SupportMessagingFacade(
    private val supportMessagingService: SupportMessagingService,
) {
    fun getSupportThreads(actor: SupportActor): List<SupportThreadDto> =
        supportMessagingService
            .listThreadWithMessages(actor, Pageable.unpaged())
            .content
            .map { entry ->
                SupportThreadDto(
                    id = requireNotNull(entry.thread.id),
                    messages = entry.messages.map { it.text },
                )
            }

    fun getSupportThreadSummaries(
        actor: SupportActor,
        pageable: Pageable,
    ): Page<SupportThreadSummaryDto> =
        supportMessagingService.listVisibleThreads(actor, pageable).map { it.toSummaryDto() }

    fun createSupportThread(
        actor: SupportActor,
        request: CreateSupportThreadRequestDto,
    ): SupportThreadSummaryDto =
        supportMessagingService.createThread(actor, request.initialMessage.trim()).toSummaryDto()

    fun getSupportThreadMessages(
        actor: SupportActor,
        threadId: UUID,
        pageable: Pageable,
    ): Page<SupportMessageDto> = supportMessagingService.getThreadMessages(actor, threadId, pageable).map { it.toDto() }

    fun addSupportThreadMessage(
        actor: SupportActor,
        threadId: UUID,
        request: CreateSupportMessageRequestDto,
    ): SupportMessageDto = supportMessagingService.addThreadMessage(actor, threadId, request.text.trim()).toDto()

    private fun SupportThreadPersistence.toSummaryDto(): SupportThreadSummaryDto =
        SupportThreadSummaryDto(
            id = requireNotNull(id),
            ownerUserId = ownerUserId,
            createdAt = createdAt,
            lastMessageAt = updatedAt,
        )

    private fun SupportMessagePersistence.toDto(): SupportMessageDto =
        SupportMessageDto(
            id = requireNotNull(id),
            threadId = threadId,
            authorUserId = authorUserId,
            authorRole = authorRole.toDto(),
            text = text,
            createdAt = createdAt,
        )

    private fun SupportParticipantRoleEnumPersistence.toDto(): SupportParticipantRoleEnumDto =
        when (this) {
            SupportParticipantRoleEnumPersistence.USER -> SupportParticipantRoleEnumDto.USER
            SupportParticipantRoleEnumPersistence.SUPPORT -> SupportParticipantRoleEnumDto.SUPPORT
        }
}