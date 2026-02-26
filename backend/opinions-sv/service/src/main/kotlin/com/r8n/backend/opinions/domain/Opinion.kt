package com.r8n.backend.opinions.domain

import java.time.Instant
import java.util.UUID

data class Opinion(
    val id: UUID,
    val owner: UUID,
    val subject: UUID,
    val subjective: List<String>,
    val objective: List<String>,
    val mark: Double?,
    val componentMark: Double?,
    val components: List<WeightedOpinionReference>,
    val status: OpinionStatusEnum,
    val timestamp: Instant,
)
