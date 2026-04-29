package com.r8n.backend.messaging.service

import com.r8n.backend.messaging.persistence.SupportMessagePersistence
import com.r8n.backend.messaging.persistence.SupportParticipantRoleEnumPersistence
import com.r8n.backend.messaging.persistence.SupportThreadPersistence
import com.r8n.backend.messaging.provider.database.SupportMessageRepository
import com.r8n.backend.messaging.provider.database.SupportThreadRepository
import com.r8n.backend.messaging.provider.database.SupportThreadSummaryProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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
    fun listThreadWithMessages(
        actor: SupportActor,
        pageable: Pageable,
    ): Page<SupportThreadWithMessages> {
        val visibleThreads = listVisibleThreads(actor, pageable)
        val threadIds = visibleThreads.content.mapNotNull { it.id }
        val messagesByThreadId =
            supportMessageRepository
                .findAllByThreadIdInOrderByThreadIdAscCreatedAtAsc(threadIds)
                .groupBy { it.threadId }

        return PageImpl(
            visibleThreads.content.map { thread ->
                SupportThreadWithMessages(
                    thread = thread,
                    messages = messagesByThreadId[requireNotNull(thread.id)].orEmpty(),
                )
            },
            visibleThreads.pageable,
            visibleThreads.totalElements,
        )
    }

    fun listVisibleThreads(
        actor: SupportActor,
        pageable: Pageable,
    ): Page<SupportThreadPersistence> =
        supportThreadRepository.findVisibleOrderByLastMessageAtDesc(
            ownerUserId =
                actor.userId.takeUnless {
                    actor.role == SupportParticipantRoleEnumPersistence.SUPPORT
                },
            pageable = pageable,
        )

    fun listThreadSummaries(
        actor: SupportActor,
        pageable: Pageable,
    ): Page<SupportThreadSummary> {
        val summaries =
            supportThreadRepository
                .findVisibleSummariesOrderByLastMessageAtDesc(
                    ownerUserId =
                        actor.userId.takeUnless {
                            actor.role == SupportParticipantRoleEnumPersistence.SUPPORT
                        },
                    pageable = pageable,
                )
        if (summaries.isEmpty) {
            return summaries.map { it.toSummary(lastMessageText = "") }
        }

        val unreadCountsByThreadId =
            if (actor.role == SupportParticipantRoleEnumPersistence.SUPPORT) {
                supportMessageRepository
                    .countUnreadUserMessagesByThreadId(summaries.content.map { it.id })
                    .associate { it.threadId to it.unreadCount }
            } else {
                emptyMap()
            }
        val messagesByThreadId =
            supportMessageRepository
                .findAllByThreadIdInOrderByThreadIdAscCreatedAtAsc(summaries.content.map { it.id })
                .groupBy { it.threadId }

        return summaries.map { summary ->
            summary.toSummary(
                lastMessageText =
                    messagesByThreadId[summary.id]
                        ?.maxByOrNull { it.createdAt }
                        ?.text
                        .orEmpty(),
                unreadCount = unreadCountsByThreadId[summary.id] ?: 0,
            )
        }
    }

    fun countUnreadMessages(actor: SupportActor): Long =
        if (actor.role == SupportParticipantRoleEnumPersistence.SUPPORT) {
            supportMessageRepository.countUnreadUserMessages()
        } else {
            0
        }

    @Transactional
    fun createThread(
        actor: SupportActor,
        initialMessageText: String,
    ): SupportThreadWithMessages {
        if (initialMessageText.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial message cannot be blank")
        }

        val now = Instant.now()
        val thread =
            supportThreadRepository.findFirstByOwnerUserId(actor.userId)
                ?: supportThreadRepository.save(
                    SupportThreadPersistence(
                        ownerUserId = actor.userId,
                    ),
                )

        supportMessageRepository.save(
            SupportMessagePersistence(
                threadId = requireNotNull(thread.id),
                authorUserId = actor.userId,
                authorRole = SupportParticipantRoleEnumPersistence.USER,
                text = initialMessageText,
                createdAt = now,
            ),
        )

        return SupportThreadWithMessages(
            thread = thread,
            messages =
                supportMessageRepository.findAllByThreadIdInOrderByThreadIdAscCreatedAtAsc(
                    listOf(requireNotNull(thread.id)),
                ),
        )
    }

    @Transactional
    fun getThreadMessages(
        actor: SupportActor,
        threadId: UUID,
        pageable: Pageable,
    ): Page<SupportMessagePersistence> {
        val thread = findThreadVisibleForActor(actor, threadId)
        if (actor.readsAsSupportTeam(thread)) {
            supportMessageRepository.markUnreadUserMessagesReadBySupport(
                threadId = requireNotNull(thread.id),
                readerUserId = actor.userId,
                readAt = Instant.now(),
            )
        }
        return supportMessageRepository.findAllByThreadIdOrderByCreatedAtAsc(requireNotNull(thread.id), pageable)
    }

    @Transactional
    fun deleteThread(
        actor: SupportActor,
        threadId: UUID,
    ) {
        val thread = findThreadVisibleForActor(actor, threadId)
        supportMessageRepository.deleteAllByThreadId(requireNotNull(thread.id))
        supportThreadRepository.delete(thread)
    }

    @Transactional
    fun addThreadMessage(
        actor: SupportActor,
        threadId: UUID,
        text: String,
    ): SupportMessagePersistence {
        if (text.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Message text cannot be blank")
        }

        val thread = findThreadVisibleForActor(actor, threadId)
        val authorRole =
            if (thread.ownerUserId == actor.userId) {
                SupportParticipantRoleEnumPersistence.USER
            } else {
                actor.role
            }
        val now = Instant.now()
        val message =
            supportMessageRepository.save(
                SupportMessagePersistence(
                    threadId = requireNotNull(thread.id),
                    authorUserId = actor.userId,
                    authorRole = authorRole,
                    text = text,
                    createdAt = now,
                ),
            )
        return message
    }

    private fun findThreadVisibleForActor(
        actor: SupportActor,
        threadId: UUID,
    ): SupportThreadPersistence {
        val thread =
            supportThreadRepository.findById(threadId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Support thread not found")
            }

        if (actor.role != SupportParticipantRoleEnumPersistence.SUPPORT && thread.ownerUserId != actor.userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Support thread belongs to another user")
        }
        return thread
    }

    private fun SupportActor.readsAsSupportTeam(thread: SupportThreadPersistence): Boolean =
        role == SupportParticipantRoleEnumPersistence.SUPPORT && thread.ownerUserId != userId
}

data class SupportThreadWithMessages(
    val thread: SupportThreadPersistence,
    val messages: List<SupportMessagePersistence>,
)

data class SupportThreadSummary(
    val id: UUID,
    val ownerUserId: UUID,
    val createdAt: Instant,
    val lastMessageAt: Instant,
    val lastMessageText: String,
    val unreadCount: Long = 0,
)

private fun SupportThreadSummaryProjection.toSummary(
    lastMessageText: String,
    unreadCount: Long = 0,
): SupportThreadSummary =
    SupportThreadSummary(
        id = id,
        ownerUserId = ownerUserId,
        createdAt = createdAt,
        lastMessageAt = lastMessageAt,
        lastMessageText = lastMessageText,
        unreadCount = unreadCount,
    )
