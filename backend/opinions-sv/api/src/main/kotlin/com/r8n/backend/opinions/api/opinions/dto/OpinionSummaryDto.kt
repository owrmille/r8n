package com.r8n.backend.opinions.api.opinions.dto

import java.util.UUID

data class OpinionSummaryDto(
    val subject: UUID,
    val subjectName: String,
    val ownMark: Double?,
    val componentMark: Double?,
    val opinions: List<WeightedOpinionReferenceDto>,
)