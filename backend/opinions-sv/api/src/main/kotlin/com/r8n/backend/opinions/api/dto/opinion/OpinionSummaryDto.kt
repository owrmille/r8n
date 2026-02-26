package com.r8n.backend.opinions.api.dto.opinion

import java.util.UUID

data class OpinionSummaryDto(
    val id: UUID,
    val subject: UUID,
    val subjectName: String,
    val ownMark: Double?,
    val synchronizedMark: Double,
    val componentMark: Double?,
    val opinions: List<WeightedOpinionReferenceDto>,
)