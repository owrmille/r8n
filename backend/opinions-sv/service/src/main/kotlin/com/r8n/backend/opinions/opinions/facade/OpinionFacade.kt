package com.r8n.backend.opinions.opinions.facade

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.core.utils.toPageable
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import com.r8n.backend.opinions.opinions.service.OpinionService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OpinionFacade(
    private val opinionService: OpinionService,
    private val opinionMapper: OpinionMapper,
) {
    fun getOpinion(
        opinionId: UUID,
        requesterId: UUID,
    ): OpinionDto = opinionMapper.toDto(opinionService.getOpinion(opinionId, requesterId))

    fun getOpinionFor(
        subjectId: UUID,
        requesterId: UUID,
    ): OpinionDto = opinionMapper.toDto(opinionService.getOpinionFor(subjectId, requesterId))

    fun createOpinion(
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
        creatorId: UUID,
    ): OpinionDto = opinionMapper.toDto(opinionService.createOpinion(subjectId, subjective, objective, mark, creatorId))

    fun updateOpinion(
        opinionId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
        ownerId: UUID,
    ): OpinionDto = opinionMapper.toDto(opinionService.updateOpinion(opinionId, subjective, objective, mark, ownerId))

    fun getMyFullOpinions(
        ownerId: UUID,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionDto> =
        opinionService
            .getMyFullOpinions(ownerId, pageable.toPageable())
            .map { opinionMapper.toDto(it) }
            .toResponse()

    fun deleteOpinion(
        opinionId: UUID,
        ownerId: UUID,
    ) {
        opinionService.deleteOpinion(opinionId, ownerId)
    }

    fun linkComponent(
        parentOpinionId: UUID,
        childOpinionId: UUID,
        weight: Double,
        ownerId: UUID,
    ): OpinionDto = opinionMapper.toDto(opinionService.linkComponent(parentOpinionId, childOpinionId, weight, ownerId))

    fun unlinkComponent(
        linkId: UUID,
        ownerId: UUID,
    ): OpinionDto = opinionMapper.toDto(opinionService.unlinkComponent(linkId, ownerId))

    fun adjustComponentWeight(
        linkId: UUID,
        weight: Double,
        ownerId: UUID,
    ): OpinionDto = opinionMapper.toDto(opinionService.adjustComponentWeight(linkId, weight, ownerId))

    fun restoreOpinion(dto: OpinionDto) {
        opinionService.restoreOpinion(dto)
    }
}
