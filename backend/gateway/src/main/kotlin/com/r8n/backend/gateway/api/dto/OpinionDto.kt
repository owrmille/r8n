package com.r8n.backend.gateway.api.dto

import com.r8n.backend.gateway.api.dto.OpinionStatusEnumDto
import java.time.Instant
import java.util.UUID

class OpinionDto(
	val id: UUID,
	val owner: UUID,
	val ownerName: String,
	val subject: UUID,
	val subjectName: String,
	val subjective: List<String>,
	val objective: List<String>,
	val mark: Double?,
	val componentWeightedMark: Double,
	val components: List<WeightedOpinionDto>,
	val status: OpinionStatusEnumDto,
	val timestamp: Instant,
)

class WeightedOpinionDto(
	val id: UUID,
	val opinion: UUID,
	val weight: Double,
)