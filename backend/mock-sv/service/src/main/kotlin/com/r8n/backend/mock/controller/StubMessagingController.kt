package com.r8n.backend.mock.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.dto.SupportThreadDto
import com.r8n.backend.mock.api.dto.messaging.CreateSupportMessageRequestDto
import com.r8n.backend.mock.api.dto.messaging.CreateSupportThreadRequestDto
import com.r8n.backend.mock.api.dto.messaging.SupportMessageDto
import com.r8n.backend.mock.api.dto.messaging.SupportParticipantRoleEnumDto
import com.r8n.backend.mock.api.dto.messaging.SupportThreadSummaryDto
import com.r8n.backend.security.Authority
import com.r8n.backend.security.Authority.IS_USER_OR_SUPPORT
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@RestController
class StubMessagingController : MessagingApi {
    private data class SupportThreadAggregate(
        var summary: SupportThreadSummaryDto,
        val messages: MutableList<SupportMessageDto>,
    )

    private val threads: MutableMap<UUID, SupportThreadAggregate> = ConcurrentHashMap()

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun getSupportThreads() =
        PageImpl(
            listVisibleThreads().map { entry ->
                SupportThreadDto(
                    id = entry.summary.id,
                    messages = entry.messages.map { it.text },
                )
            },
        ).toResponse()

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun getSupportThreadSummaries(pageable: PageRequestDto) =
        PageImpl(listVisibleThreads().map { it.summary }).toResponse()

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun createSupportThread(request: CreateSupportThreadRequestDto): SupportThreadSummaryDto {
        val initialMessageText = request.initialMessage.trim()
        if (initialMessageText.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial message cannot be blank")
        }

        val author = getAuthenticatedUserContext()
        val threadId = UUID.randomUUID()
        val now = Instant.now()
        val message =
            SupportMessageDto(
                id = UUID.randomUUID(),
                threadId = threadId,
                authorUserId = author.userId,
                authorRole = author.role,
                text = initialMessageText,
                createdAt = now,
            )
        val summary =
            SupportThreadSummaryDto(
                id = threadId,
                ownerUserId = author.userId,
                createdAt = now,
                updatedAt = now,
                lastMessageAt = now,
            )
        threads[threadId] = SupportThreadAggregate(summary = summary, messages = mutableListOf(message))
        return summary
    }

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun getSupportThreadMessages(
        threadId: UUID,
        pageable: PageRequestDto,
    ) = PageImpl(findThreadVisibleForCurrentUser(threadId).messages.toList()).toResponse()

    @PreAuthorize(IS_USER_OR_SUPPORT)
    override fun addSupportThreadMessage(
        threadId: UUID,
        request: CreateSupportMessageRequestDto,
    ): SupportMessageDto {
        val text = request.text.trim()
        if (text.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Message text cannot be blank")
        }

        val author = getAuthenticatedUserContext()
        val thread = findThreadVisibleForCurrentUser(threadId)
        val now = Instant.now()
        val message =
            SupportMessageDto(
                id = UUID.randomUUID(),
                threadId = threadId,
                authorUserId = author.userId,
                authorRole = author.role,
                text = text,
                createdAt = now,
            )
        thread.messages += message
        thread.summary =
            thread.summary.copy(
                updatedAt = now,
                lastMessageAt = now,
            )
        return message
    }

    private fun listVisibleThreads(): List<SupportThreadAggregate> {
        val auth = getAuthenticatedUserContext()
        return threads.values
            .filter { auth.isSupport || it.summary.ownerUserId == auth.userId }
            .sortedByDescending { it.summary.updatedAt }
    }

    private fun findThreadVisibleForCurrentUser(threadId: UUID): SupportThreadAggregate {
        val thread =
            threads[threadId] ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Support thread not found",
            )

        val auth = getAuthenticatedUserContext()
        if (!auth.isSupport && thread.summary.ownerUserId != auth.userId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Support thread not found")
        }
        return thread
    }

    private fun getAuthenticatedUserContext(): AuthenticatedUserContext {
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
        return AuthenticatedUserContext(userId = userId, isSupport = isSupport, role = role)
    }

    private data class AuthenticatedUserContext(
        val userId: UUID,
        val isSupport: Boolean,
        val role: SupportParticipantRoleEnumDto,
    )
}