package com.r8n.backend.messaging.service

import com.r8n.backend.messaging.persistence.SupportMessagePersistence
import com.r8n.backend.messaging.persistence.SupportThreadPersistence
import com.r8n.backend.messaging.provider.database.SupportMessageRepository
import com.r8n.backend.messaging.provider.database.SupportThreadRepository
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
        if (actor.isSupport) {
            supportThreadRepository.findAllByOrderByUpdatedAtDesc(pageable)
        } else {
            supportThreadRepository.findAllByOwnerUserIdOrderByUpdatedAtDesc(actor.userId, pageable)
        }

    @Transactional
    fun createThread(
        actor: SupportActor,
        initialMessageText: String,
    ): SupportThreadPersistence {
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
                authorRole = actor.role,
                text = initialMessageText,
                createdAt = now,
            ),
        )
        return thread
    }

    fun getThreadMessages(
        actor: SupportActor,
        threadId: UUID,
        pageable: Pageable,
    ): Page<SupportMessagePersistence> {
        val thread = findThreadVisibleForActor(actor, threadId)
        return supportMessageRepository.findAllByThreadIdOrderByCreatedAtAsc(requireNotNull(thread.id), pageable)
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
        val now = Instant.now()
        val message =
            supportMessageRepository.save(
                SupportMessagePersistence(
                    threadId = requireNotNull(thread.id),
                    authorUserId = actor.userId,
                    authorRole = actor.role,
                    text = text,
                    createdAt = now,
                ),
            )

        thread.updatedAt = now
        supportThreadRepository.save(thread)
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

        if (!actor.isSupport && thread.ownerUserId != actor.userId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Support thread not found")
        }
        return thread
    }
}

data class SupportThreadWithMessages(
    val thread: SupportThreadPersistence,
    val messages: List<SupportMessagePersistence>,
)