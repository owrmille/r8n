package com.r8n.backend.opinions.access.database

import com.r8n.backend.opinions.access.domain.RequestStatusEnum
import com.r8n.backend.opinions.access.persistence.AccessRequestPersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface AccessRequestRepository : JpaRepository<AccessRequestPersistence, UUID> {
    fun findByRequesterAndListAndStatusIn(
        requesterId: UUID,
        listId: UUID,
        statuses: List<RequestStatusEnum>,
    ): List<AccessRequestPersistence>

    @Query(
        """
        SELECT ar FROM AccessRequestPersistence ar
        WHERE (:listId IS NULL OR ar.list = :listId)
        AND (:requesterId IS NULL OR ar.requester = :requesterId)
        AND (:status IS NULL OR ar.status = :status)
    """,
    )
    fun findAllByFilters(
        listId: UUID?,
        requesterId: UUID?,
        status: RequestStatusEnum?,
        pageable: Pageable,
    ): Page<AccessRequestPersistence>

    fun existsByRequesterAndListAndStatus(
        requester: UUID,
        list: UUID,
        status: RequestStatusEnum,
    ): Boolean
}