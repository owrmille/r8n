package com.r8n.backend.mock.service

import com.r8n.backend.mock.api.dto.SupportThreadDto
import com.r8n.backend.mock.api.dto.messaging.CreateSupportMessageRequestDto
import com.r8n.backend.mock.api.dto.messaging.CreateSupportThreadRequestDto
import com.r8n.backend.mock.api.dto.messaging.SupportMessageDto
import com.r8n.backend.mock.api.dto.messaging.SupportParticipantRoleEnumDto
import com.r8n.backend.mock.api.dto.messaging.SupportThreadSummaryDto
import com.r8n.backend.mock.persistence.SupportMessagePersistence
import com.r8n.backend.mock.persistence.SupportParticipantRoleEnumPersistence
import com.r8n.backend.mock.persistence.SupportThreadPersistence
import com.r8n.backend.mock.provider.database.SupportMessageRepository
import com.r8n.backend.mock.provider.database.SupportThreadRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class SupportMessagingService(
    private val supportThreadRepository: SupportThreadRepository,
    private val supportMessageRepository: SupportMessageRepository,
) {
    fun getSupportThreads(actor: SupportActor): List<SupportThreadDto> =
        listVisibleThreadSummaries(actor).map { summary ->
            SupportThreadDto(
                id = summary.id,
                messages = getSupportThreadMessages(actor, summary.id).map { it.text },
            )
        }

    fun listVisibleThreadSummaries(actor: SupportActor): List<SupportThreadSummaryDto> {
        val threads =
            if (actor.isSupport) {
                supportThreadRepository.findAll().sortedByDescending { it.updatedAt }
            } else {
                supportThreadRepository.findAllByOwnerUserIdOrderByUpdatedAtDesc(actor.userId)
            }
        return threads.map { it.toSummaryDto() }
    }

    @Transactional
    fun createSupportThread(
        actor: SupportActor,
        request: CreateSupportThreadRequestDto,
    ): SupportThreadSummaryDto {
        val initialMessageText = request.initialMessage.trim()
        if (initialMessageText.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial message cannot be blank")
        }

        val now = Instant.now()
        val thread =
            supportThreadRepository.save(
                SupportThreadPersistence(
                    ownerUserId = actor.userId,
                    createdAt = now,
                    updatedAt = now,
                ),
            )

        supportMessageRepository.save(
            SupportMessagePersistence(
                threadId = requireNotNull(thread.id),
                authorUserId = actor.userId,
                authorRole = actor.role.toPersistence(),
                text = initialMessageText,
                createdAt = now,
            ),
        )

        return thread.toSummaryDto(lastMessageAt = now)
    }

    fun getSupportThreadMessages(
        actor: SupportActor,
        threadId: UUID,
    ): List<SupportMessageDto> {
        val thread = findThreadVisibleForActor(actor, threadId)
        val storedMessages = supportMessageRepository.findAllByThreadIdOrderByCreatedAtAsc(requireNotNull(thread.id))
        return storedMessages.map { it.toDto() }
    }

    @Transactional
    fun addSupportThreadMessage(
        actor: SupportActor,
        threadId: UUID,
        request: CreateSupportMessageRequestDto,
    ): SupportMessageDto {
        val text = request.text.trim()
        if (text.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Message text cannot be blank")
        }

        val thread = findThreadVisibleForActor(actor, threadId)
        val now = Instant.now()
        val savedMessage =
            supportMessageRepository.save(
                SupportMessagePersistence(
                    threadId = requireNotNull(thread.id),
                    authorUserId = actor.userId,
                    authorRole = actor.role.toPersistence(),
                    text = text,
                    createdAt = now,
                ),
            )

        thread.updatedAt = now
        supportThreadRepository.save(thread)
        return savedMessage.toDto()
    }

    private fun findThreadVisibleForActor(
        actor: SupportActor,
        threadId: UUID,
    ): SupportThreadPersistence {
        val thread =
            supportThreadRepository.findById(threadId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Support thread not found")
            }

        if (!actor.isSupport && thread.ownerUserId != actor.userId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Support thread not found")
        }
        return thread
    }

    private fun SupportThreadPersistence.toSummaryDto(lastMessageAt: Instant? = updatedAt): SupportThreadSummaryDto =
        SupportThreadSummaryDto(
            id = requireNotNull(id),
            ownerUserId = ownerUserId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastMessageAt = lastMessageAt,
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

    private fun SupportParticipantRoleEnumDto.toPersistence(): SupportParticipantRoleEnumPersistence =
        SupportParticipantRoleEnumPersistence.valueOf(name)

    private fun SupportParticipantRoleEnumPersistence.toDto(): SupportParticipantRoleEnumDto =
        SupportParticipantRoleEnumDto.valueOf(name)
}

data class SupportActor(
    val userId: UUID,
    val isSupport: Boolean,
    val role: SupportParticipantRoleEnumDto,
)