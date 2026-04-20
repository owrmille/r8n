package com.r8n.backend.opinions.lists.domain

import com.r8n.backend.opinions.opinions.domain.WeightedOpinionReference
import java.util.UUID

data class OpinionSummary(
    val subject: UUID,
    val ownMark: Double?,
    val componentMark: Double?,
    val opinions: List<WeightedOpinionReference>,
)