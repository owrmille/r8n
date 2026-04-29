package com.r8n.backend.messaging.facade

import com.r8n.backend.messaging.api.dto.messaging.CreateDirectConversationRequestDto
import com.r8n.backend.messaging.api.dto.messaging.CreateDirectMessageRequestDto
import com.r8n.backend.messaging.api.dto.messaging.DirectConversationSummaryDto
import com.r8n.backend.messaging.api.dto.messaging.DirectMessageDto
import com.r8n.backend.messaging.api.dto.messaging.MessageAuthorRoleEnumDto
import com.r8n.backend.messaging.persistence.ConversationPersistence
import com.r8n.backend.messaging.persistence.MessageAuthorRoleEnumPersistence
import com.r8n.backend.messaging.persistence.MessagePersistence
import com.r8n.backend.messaging.provider.database.ConversationParticipantRepository
import com.r8n.backend.messaging.provider.database.MessageRepository
import com.r8n.backend.messaging.service.DirectActor
import com.r8n.backend.messaging.service.DirectConversationWithMessages
import com.r8n.backend.messaging.service.DirectMessagingService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DirectMessagingFacade(
    private val directMessagingService: DirectMessagingService,
    private val conversationParticipantRepository: ConversationParticipantRepository,
    private val messageRepository: MessageRepository,
    private val usersInternalApi: UsersInternalApi,
) {
    fun getDirectConversationSummaries(
        actor: DirectActor,
        pageable: Pageable,
    ): Page<DirectConversationSummaryDto> {
        val conversations = directMessagingService.listVisibleConversations(actor, pageable)
        val conversationIds = conversations.content.mapNotNull { it.id }
        val participantsByConversationId =
            conversationParticipantRepository
                .findAllByConversationIdIn(conversationIds)
                .groupBy { it.conversationId }
        val messagesByConversationId =
            messageRepository
                .findAllByConversationIdInOrderByConversationIdAscCreatedAtAsc(conversationIds)
                .groupBy { it.conversationId }
        val displayNamesByUserId = mutableMapOf<UUID, String>()

        return conversations.map { conversation ->
            conversation.toSummaryDto(
                actor = actor,
                participantUserIds = participantsByConversationId[requireNotNull(conversation.id)].orEmpty().map { it.userId },
                messages = messagesByConversationId[conversation.id].orEmpty(),
                displayNamesByUserId = displayNamesByUserId,
            )
        }
    }

    fun createDirectConversation(
        actor: DirectActor,
        request: CreateDirectConversationRequestDto,
    ): DirectConversationSummaryDto =
        directMessagingService
            .createConversation(actor, request.recipientUserId, request.initialMessage.trim())
            .toSummaryDto(actor)

    fun getDirectConversationMessages(
        actor: DirectActor,
        conversationId: UUID,
        pageable: Pageable,
    ): Page<DirectMessageDto> =
        directMessagingService.getConversationMessages(actor, conversationId, pageable).map { it.toDto() }

    fun addDirectConversationMessage(
        actor: DirectActor,
        conversationId: UUID,
        request: CreateDirectMessageRequestDto,
    ): DirectMessageDto =
        directMessagingService
            .addConversationMessage(actor, conversationId, request.text.trim())
            .toDto()

    private fun DirectConversationWithMessages.toSummaryDto(actor: DirectActor): DirectConversationSummaryDto =
        conversation.toSummaryDto(
            actor = actor,
            participantUserIds = participants.map { it.userId },
            messages = messages,
            displayNamesByUserId = mutableMapOf(),
        )

    private fun ConversationPersistence.toSummaryDto(
        actor: DirectActor,
        participantUserIds: List<UUID>,
        messages: List<MessagePersistence>,
        displayNamesByUserId: MutableMap<UUID, String>,
    ): DirectConversationSummaryDto {
        val otherParticipantId =
            participantUserIds.firstOrNull { it != actor.userId }
                ?: actor.userId
        val latestMessage = messages.maxByOrNull { it.createdAt }
        return DirectConversationSummaryDto(
            id = requireNotNull(id),
            participantUserId = otherParticipantId,
            participantDisplayName =
                displayNamesByUserId.getOrPut(otherParticipantId) {
                    usersInternalApi.getUserName(otherParticipantId)
                },
            createdAt = createdAt,
            lastMessageAt = lastMessageAt,
            lastMessageText = latestMessage?.text,
        )
    }

    private fun MessagePersistence.toDto(): DirectMessageDto =
        DirectMessageDto(
            id = requireNotNull(id),
            conversationId = conversationId,
            authorUserId = authorUserId,
            authorDisplayName = authorDisplayNameSnapshot,
            authorRole = authorRoleSnapshot.toDto(),
            text = text,
            createdAt = createdAt,
        )

    private fun MessageAuthorRoleEnumPersistence.toDto(): MessageAuthorRoleEnumDto =
        when (this) {
            MessageAuthorRoleEnumPersistence.USER -> MessageAuthorRoleEnumDto.USER
            MessageAuthorRoleEnumPersistence.MODERATOR -> MessageAuthorRoleEnumDto.MODERATOR
            MessageAuthorRoleEnumPersistence.SUPPORT -> MessageAuthorRoleEnumDto.SUPPORT
            MessageAuthorRoleEnumPersistence.ADMIN -> MessageAuthorRoleEnumDto.ADMIN
        }
}
