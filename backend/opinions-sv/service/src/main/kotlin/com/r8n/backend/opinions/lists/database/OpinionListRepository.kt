package com.r8n.backend.opinions.lists.database

import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
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

    fun findAllByOwner(owner: UUID): List<OpinionListPersistence>
}