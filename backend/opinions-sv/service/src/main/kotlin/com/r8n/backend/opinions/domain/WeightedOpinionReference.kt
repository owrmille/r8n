package com.r8n.backend.opinions.domain

import com.r8n.backend.opinions.api.dto.WeightedOpinionReferenceDto
import java.util.UUID

data class WeightedOpinionReference(
    val id: UUID,
    val opinion: UUID,
    val weight: Double,
)

fun WeightedOpinionReference.toDto() = WeightedOpinionReferenceDto(
    id,
    opinion,
    weight,
)

fun WeightedOpinionReferenceDto.fromDto() = WeightedOpinionReference(
    id,
    opinion,
    weight,
)
