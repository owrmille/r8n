package com.r8n.backend.opinions.lists.domain

import java.util.UUID

data class OpinionList(
    val id: UUID? = null, // null for the virtual list of all my opinions
    val name: String,
    val owner: UUID,
    val opinionSummaries: List<OpinionSummary>,
    val privacy: OpinionListPrivacyEnum,
)
