package com.r8n.backend.opinions.facade

import com.r8n.backend.mock.integration.UserClient
import com.r8n.backend.opinions.api.dto.opinion.OpinionDto
import com.r8n.backend.opinions.domain.toDto
import com.r8n.backend.opinions.service.OpinionService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OpinionFacade(
    private val opinionService: OpinionService,
    private val userClient: UserClient,
) {
    fun getOpinionDto(opinionId: UUID): OpinionDto {
        val opinion = opinionService.getOpinion(opinionId)
        return OpinionDto(
            opinion.id,
            opinion.owner,
            userClient.getUserName(opinion.owner),
            opinion.subject,
            opinion.subjectName,
            opinion.subjective,
            opinion.objective,
            opinion.mark,
            opinion.componentSection.componentMark,
            opinion.componentSection.components.map { it.toDto() }.toList(),
            opinion.status.toDto(),
            opinion.timestamp,
        )
    }
}