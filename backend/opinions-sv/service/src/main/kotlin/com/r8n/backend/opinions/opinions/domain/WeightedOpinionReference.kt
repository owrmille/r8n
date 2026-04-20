package com.r8n.backend.opinions.opinions.domain

import com.r8n.backend.opinions.api.opinions.dto.WeightedOpinionReferenceDto
import java.util.UUID

data class WeightedOpinionReference(
    val id: UUID,
    val opinion: UUID,
    val weight: Double,
)
