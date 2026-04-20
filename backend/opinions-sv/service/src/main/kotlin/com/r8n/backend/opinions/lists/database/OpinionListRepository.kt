package com.r8n.backend.opinions.lists.database

import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OpinionListRepository : JpaRepository<OpinionListPersistence, UUID> {
}