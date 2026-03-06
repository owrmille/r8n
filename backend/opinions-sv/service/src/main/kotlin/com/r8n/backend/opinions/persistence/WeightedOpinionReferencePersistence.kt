package com.r8n.backend.opinions.persistence

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "weighted_opinion_reference")
class WeightedOpinionReferencePersistence(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID,
    val parentOpinion: UUID,
    val childOpinion: UUID,
    val weight: Double,
)