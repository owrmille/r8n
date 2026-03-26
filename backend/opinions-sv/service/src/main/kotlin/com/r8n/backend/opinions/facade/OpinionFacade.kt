package com.r8n.backend.opinions.facade

import com.r8n.backend.mock.integration.UserClient
import com.r8n.backend.opinions.api.dto.opinion.OpinionDto
import com.r8n.backend.opinions.domain.Opinion
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
        return opinionService.getOpinion(opinionId).toDto()
    }

    fun getOpinionForDto(subjectId: UUID): OpinionDto {
        return opinionService.getOpinionFor(subjectId).toDto()
    }

    private fun Opinion.toDto() = OpinionDto(
        id,
        owner,
        userClient.getUserName(owner),
        subject,
        subjectName,
        subjective,
        objective,
        mark,
        componentSection.componentMark,
        componentSection.components.map { it.toDto() }.toList(),
        status.toDto(),
        timestamp,
    )
}
