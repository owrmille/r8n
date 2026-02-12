package com.r8n.backend.gateway.api.dto.opinion

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
	val componentMark: Double?,
	val components: List<WeightedOpinionReferenceDto>,
	val status: OpinionStatusEnumDto,
	val timestamp: Instant,
)

class WeightedOpinionReferenceDto(
	val id: UUID,
	val opinion: UUID,
	val weight: Double,
)
