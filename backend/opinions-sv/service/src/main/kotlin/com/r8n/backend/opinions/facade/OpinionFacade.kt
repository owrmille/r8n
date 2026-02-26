package com.r8n.backend.opinions.facade

import com.r8n.backend.mock.integration.UserClient
import com.r8n.backend.opinions.api.dto.opinion.OpinionDto
import com.r8n.backend.opinions.service.OpinionService
import java.util.UUID

class OpinionFacade(
    private val opinionService: OpinionService,
    private val userClient: UserClient,
) {
    fun getOpinionDto(opinionId: UUID): OpinionDto {
        val opinion = opinionService.getOpinion(opinionId)
        return OpinionDto(
            opinion.id,
            opinion.owner,
            userClient.getUserSummary(opinion.owner).name,
            opinion.subject,

        )
    }
}