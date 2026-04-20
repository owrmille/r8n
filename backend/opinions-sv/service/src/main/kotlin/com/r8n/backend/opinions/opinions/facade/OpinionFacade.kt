package com.r8n.backend.opinions.opinions.facade

import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import com.r8n.backend.opinions.opinions.service.OpinionService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OpinionFacade(
    private val opinionService: OpinionService,
    private val opinionMapper: OpinionMapper,
) {
    fun getOpinion(opinionId: UUID): OpinionDto = opinionMapper.toDto(opinionService.getOpinion(opinionId))

    fun getOpinionFor(subjectId: UUID): OpinionDto = opinionMapper.toDto(opinionService.getMyOpinionFor(subjectId))

    fun createOpinion(
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): OpinionDto = opinionMapper.toDto(opinionService.createOpinion(subjectId, subjective, objective, mark))

    fun updateOpinion(
        opinionId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): OpinionDto = opinionMapper.toDto(opinionService.updateOpinion(opinionId, subjective, objective, mark))

    fun deleteOpinion(opinionId: UUID) {
        opinionService.deleteOpinion(opinionId)
    }

    fun linkComponent(
        parentOpinionId: UUID,
        childOpinionId: UUID,
        weight: Double,
    ): OpinionDto = opinionMapper.toDto(opinionService.linkComponent(parentOpinionId, childOpinionId, weight))

    fun unlinkComponent(linkId: UUID): OpinionDto = opinionMapper.toDto(opinionService.unlinkComponent(linkId))

    fun adjustComponentWeight(
        linkId: UUID,
        weight: Double,
    ): OpinionDto = opinionMapper.toDto(opinionService.adjustComponentWeight(linkId, weight))
}