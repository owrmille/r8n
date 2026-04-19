package com.r8n.backend.opinionlists.api.dto

import java.util.UUID

data class OpinionListSummaryDto(
    val id: UUID,
    val listName: String,
    val owner: UUID,
    val ownerName: String,
    val opinionsCount: Long,
    val grantedAccessCount: Int,
    val privacy: OpinionListPrivacyEnumDto,
)