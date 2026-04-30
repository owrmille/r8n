package com.r8n.backend.opinions.api.lists.dto

import com.r8n.backend.opinions.api.opinions.dto.OpinionSummaryDto
import java.util.UUID

data class OpinionListDto(
    val id: UUID?, // null for the virtual list of all my opinions
    val listName: String,
    var owner: UUID,
    val ownerName: String,
    val opinionSummaries: List<OpinionSummaryDto>,
    val privacy: OpinionListPrivacyEnumDto,
)
