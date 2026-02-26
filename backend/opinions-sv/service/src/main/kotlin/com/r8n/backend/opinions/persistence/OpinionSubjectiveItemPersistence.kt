package com.r8n.backend.opinions.persistence

import jakarta.persistence.Entity
import java.util.UUID

@Entity
class OpinionNotePersistence(
    val id: UUID,
    val opinionId: UUID,
    val type: OpinionNoteTypeEnum,
    val description: String,
)

enum class OpinionNoteTypeEnum {
    OBJECTIVE,
    SUBJECTIVE,
}