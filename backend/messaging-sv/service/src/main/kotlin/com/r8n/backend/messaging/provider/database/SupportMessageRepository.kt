package com.r8n.backend.messaging.provider.database

import com.r8n.backend.messaging.persistence.SupportMessagePersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface SupportMessageRepository : JpaRepository<SupportMessagePersistence, UUID> {
    fun findAllByThreadIdOrderByCreatedAtAsc(
        threadId: UUID,
        pageable: Pageable,
    ): Page<SupportMessagePersistence>

    fun findAllByThreadIdInOrderByThreadIdAscCreatedAtAsc(threadIds: Collection<UUID>): List<SupportMessagePersistence>

    fun deleteAllByThreadId(threadId: UUID)

    @Query(
        """
        SELECT message.threadId AS threadId, COUNT(message) AS unreadCount
        FROM SupportMessagePersistence message
        WHERE message.threadId IN :threadIds
          AND message.authorRole = com.r8n.backend.messaging.persistence.SupportParticipantRoleEnumPersistence.USER
          AND message.readBySupportAt IS NULL
        GROUP BY message.threadId
        """,
    )
    fun countUnreadUserMessagesByThreadId(
        @Param("threadIds")
        threadIds: Collection<UUID>,
    ): List<SupportThreadUnreadCountProjection>

    @Modifying
    @Query(
        """
        UPDATE SupportMessagePersistence message
        SET message.readBySupportAt = :readAt,
            message.readBySupportUserId = :readerUserId
        WHERE message.threadId = :threadId
          AND message.authorRole = com.r8n.backend.messaging.persistence.SupportParticipantRoleEnumPersistence.USER
          AND message.readBySupportAt IS NULL
        """,
    )
    fun markUnreadUserMessagesReadBySupport(
        @Param("threadId")
        threadId: UUID,
        @Param("readerUserId")
        readerUserId: UUID,
        @Param("readAt")
        readAt: Instant,
    ): Int
}

interface SupportThreadUnreadCountProjection {
    val threadId: UUID
    val unreadCount: Long
}
