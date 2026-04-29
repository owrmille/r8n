package com.r8n.backend.opinions.lists.database

import com.r8n.backend.opinions.lists.domain.OpinionListPrivacyEnum
import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface OpinionListRepository : JpaRepository<OpinionListPersistence, UUID> {
    fun existsByIdAndOwner(
        id: UUID,
        owner: UUID,
    ): Boolean

    fun findByOwner(
        owner: UUID,
        pageable: Pageable,
    ): Page<OpinionListPersistence>

    @Query(
        """
        SELECT ol FROM OpinionListPersistence ol
        WHERE (:nameSubstring IS NULL OR LOWER(ol.name) LIKE LOWER(CONCAT('%', :nameSubstring, '%')))
        AND (:authorId IS NULL OR ol.owner = :authorId)
        AND (
            ol.owner = :requesterId
            OR EXISTS (
                SELECT 1 FROM AccessRequestPersistence ar
                WHERE ar.list = ol.id
                AND ar.requester = :requesterId
                AND ar.status = com.r8n.backend.opinions.access.domain.RequestStatusEnum.ACCEPTED
            )
        )
        """,
    )
    fun searchAccessible(
        requesterId: UUID,
        nameSubstring: String?,
        authorId: UUID?,
        pageable: Pageable,
    ): Page<OpinionListPersistence>
}
