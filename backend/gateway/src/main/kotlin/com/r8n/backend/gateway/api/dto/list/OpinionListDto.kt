package com.r8n.backend.gateway.api.dto.list

import com.r8n.backend.gateway.api.dto.opinion.OpinionSummaryDto
import java.util.UUID

data class OpinionListDto(
    val id: UUID,
    val listName: String,
    val owner: UUID,
    val ownerName: String,
    val opinionSummaries: List<OpinionSummaryDto>,
)