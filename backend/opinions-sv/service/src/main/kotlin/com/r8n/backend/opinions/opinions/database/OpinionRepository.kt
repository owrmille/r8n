package com.r8n.backend.opinions.opinions.database

import com.r8n.backend.opinions.opinions.persistence.OpinionPersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OpinionRepository : JpaRepository<OpinionPersistence, UUID> {
    fun findFirstBySubjectOrderByTimestampDesc(subject: UUID): OpinionPersistence?
}