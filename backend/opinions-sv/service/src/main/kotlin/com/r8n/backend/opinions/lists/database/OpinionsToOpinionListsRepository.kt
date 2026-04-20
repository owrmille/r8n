package com.r8n.backend.opinions.lists.database

import com.r8n.backend.opinions.lists.persistence.OpinionsToOpinionListsPersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OpinionsToOpinionListsRepository : JpaRepository<OpinionsToOpinionListsPersistence, UUID> {
    fun findAllByOpinionList(opinionListId: UUID): List<OpinionsToOpinionListsPersistence>

    fun findByOpinion(opinionId: UUID): OpinionsToOpinionListsPersistence?
}