package com.r8n.backend.opinions.facade

import com.r8n.backend.opinions.api.dto.opinion.OpinionDto
import com.r8n.backend.opinions.domain.Opinion

class OpinionFacade {
    fun toDto(opinion: Opinion) = OpinionDto(
        opinion.id,
        opinion.owner
    )
}