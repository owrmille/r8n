package com.r8n.backend.gateway.api.dto.opinion

import java.util.UUID

class OpinionSummaryDto(
    val id: UUID,
    val subject: UUID,
    val subjectName: String,
    val ownMark: Double?,
    val synchronizedMark: Double,
    val componentMark: Double,
    val opinions: List<OpinionDto>,
)