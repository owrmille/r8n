package com.r8n.backend.opinions.provider.database

import com.r8n.backend.opinions.persistence.WeightedOpinionReferencePersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WeightedOpinionReferenceRepository : JpaRepository<WeightedOpinionReferencePersistence, UUID> {
    fun findAllByParentOpinionOrderByIdAsc(parentOpinion: UUID): List<WeightedOpinionReferencePersistence>

    fun existsByParentOpinionAndChildOpinion(
        parentOpinion: UUID,
        childOpinion: UUID,
    ): Boolean
}