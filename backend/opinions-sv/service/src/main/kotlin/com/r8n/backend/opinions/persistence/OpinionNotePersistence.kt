package com.r8n.backend.opinions.persistence

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
class OpinionNotePersistence(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID,
    val opinionId: UUID,
    val type: OpinionNoteTypeEnum,
    val description: String,
)

enum class OpinionNoteTypeEnum {
    OBJECTIVE,
    SUBJECTIVE,
}