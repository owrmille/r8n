package com.r8n.backend.gateway.api.dto

import com.r8n.backend.gateway.api.dto.OpinionStatusEnumDto
import java.time.Instant
import java.util.UUID

class OpinionListDto(
	val id: UUID,
	val listName: String,
	val owner: UUID,
	val ownerName: String,
	val opinions: List<OpinionDto>,
)