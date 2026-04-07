package com.r8n.backend.opinions.provider.database

import com.r8n.backend.opinions.persistence.ReferentPersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReferentRepository : JpaRepository<ReferentPersistence, UUID> {
    fun findAllByReferentGroupOrderByIdAsc(referentGroup: UUID): List<ReferentPersistence>
}