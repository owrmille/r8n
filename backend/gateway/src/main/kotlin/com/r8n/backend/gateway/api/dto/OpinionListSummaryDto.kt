package com.r8n.backend.gateway.api.dto

import java.util.UUID

class OpinionListSummaryDto(
	val id: UUID,
	val listName: String,
	val owner: UUID,
	val ownerName: String,
	val opinionsCount: Long,
	val privacy: OpinionListPrivacyEnumDto,
)