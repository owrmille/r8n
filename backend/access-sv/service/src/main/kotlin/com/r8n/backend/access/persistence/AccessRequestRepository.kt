package com.r8n.backend.access.persistence

import com.r8n.backend.access.domain.RequestStatusEnum
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface AccessRequestRepository : JpaRepository<AccessRequestPersistence, UUID> {
    fun findByRequesterIdAndListIdAndStatusIn(
        requesterId: UUID,
        listId: UUID,
        statuses: List<RequestStatusEnum>,
    ): List<AccessRequestPersistence>

    @Query(
        """
        SELECT ar FROM AccessRequestPersistence ar
        WHERE (:listId IS NULL OR ar.listId = :listId)
        AND (:requesterId IS NULL OR ar.requesterId = :requesterId)
        AND (:ownerId IS NULL OR ar.ownerId = :ownerId)
        AND (:status IS NULL OR ar.status = :status)
    """,
    )
    fun findAllByFilters(
        listId: UUID?,
        requesterId: UUID?,
        ownerId: UUID?,
        status: RequestStatusEnum?,
        pageable: Pageable,
    ): Page<AccessRequestPersistence>

    fun findFirstByRequesterIdAndListIdAndStatus(
        requesterId: UUID,
        listId: UUID,
        status: RequestStatusEnum,
    ): Optional<AccessRequestPersistence>
}