package com.r8n.backend.opinions.access.controller.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OpinionOwnershipRepository : JpaRepository<OpinionOwnershipPersistence, UUID> {
    fun existsByOpinionId(opinionId: UUID): Boolean
}