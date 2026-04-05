package com.r8n.backend.mock.api.dto.list

import com.r8n.backend.opinions.api.dto.OpinionSummaryDto
import java.util.UUID

data class OpinionListDto(
    val id: UUID,
    val listName: String,
    val owner: UUID,
    val ownerName: String,
    val opinionSummaries: List<OpinionSummaryDto>,
)