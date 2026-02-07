package com.r8n.backend.gateway.api.dto

import com.r8n.backend.gateway.api.dto.OpinionStatusEnumDto
import java.time.Instant
import java.util.UUID

class OpinionDto(
	val id: UUID,
	val author: UUID,
	val subjective: List<String>,
	val objective: List<String>,
	val mark: Double,
	val trust: Double,
	val status: OpinionStatusEnumDto,
	val timestamp: Instant,
)