package com.r8n.backend.opinions.persistence

import jakarta.persistence.Entity
import java.util.UUID

@Entity
class WeightedOpinionReferencePersistence(
    val id: UUID,
    val parentOpinion: UUID,
    val childOpinion: UUID,
    val weight: Double,
)