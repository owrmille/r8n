package com.r8n.backend.opinions.controller

import com.r8n.backend.mock.stub.OpinionTestDataFactory
import com.r8n.backend.opinions.api.OpinionApi
import com.r8n.backend.opinions.api.dto.OpinionDto
import com.r8n.backend.opinions.facade.OpinionFacade
import com.r8n.backend.security.Authority.IS_USER
import com.r8n.backend.security.Authority.IS_USER_OR_SERVICE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class OpinionController(
    private val opinionFacade: OpinionFacade,
) : OpinionApi {
    @PreAuthorize(IS_USER_OR_SERVICE)
    override fun getOpinionById(id: UUID) = opinionFacade.getOpinion(id)

    @PreAuthorize(IS_USER)
    override fun getOpinionFor(subjectId: UUID) = opinionFacade.getOpinionFor(subjectId)

    @PreAuthorize(IS_USER)
    override fun createOpinion(
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): OpinionDto = opinionFacade.createOpinion(subjectId, subjective, objective, mark)

    @PreAuthorize(IS_USER)
    override fun updateOpinion(
        opinionId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ) = opinionFacade.updateOpinion(opinionId, subjective, objective, mark)

    @PreAuthorize(IS_USER)
    override fun deleteOpinion(opinionId: UUID) {
        opinionFacade.deleteOpinion(opinionId)
    }

    @PreAuthorize(IS_USER)
    override fun linkComponent(
        parentOpinionId: UUID,
        childOpinionId: UUID,
        weight: Double,
    ): OpinionDto = opinionFacade.linkComponent(parentOpinionId, childOpinionId, weight)

    @PreAuthorize(IS_USER)
    override fun unlinkComponent(linkId: UUID): OpinionDto {
        val auth = SecurityContextHolder.getContext().authentication ?: throw IllegalStateException("Not authenticated")
        val userId = UUID.fromString(auth.name)
        return opinionFacade.unlinkComponent(userId, linkId)
    }

    @PreAuthorize(IS_USER)
    override fun adjustComponentWeight(
        linkId: UUID,
        weight: Double,
    ): OpinionDto = OpinionTestDataFactory.getOpinion(UUID.fromString("0"))
}