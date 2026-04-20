package com.r8n.backend.opinions.lists.domain

import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import java.util.UUID

data class OpinionSummary(
    val id: UUID,
    val subject: UUID,
    val ownMark: Double?,
    val synchronizedMark: Double,
    val componentMark: Double?,
    val opinions: List<OpinionDto>,
)