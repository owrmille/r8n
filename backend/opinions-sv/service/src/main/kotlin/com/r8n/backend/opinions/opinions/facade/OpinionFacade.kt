package com.r8n.backend.opinions.opinions.facade

import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionStatusEnumDto
import com.r8n.backend.opinions.api.opinions.dto.WeightedOpinionReferenceDto
import com.r8n.backend.opinions.opinions.domain.Opinion
import com.r8n.backend.opinions.opinions.domain.OpinionStatusEnum
import com.r8n.backend.opinions.opinions.domain.WeightedOpinionReference
import com.r8n.backend.opinions.opinions.service.OpinionService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OpinionFacade(
    private val opinionService: OpinionService,
    private val usersInternalApi: UsersInternalApi,
) {
    fun getOpinion(opinionId: UUID): OpinionDto = opinionService.getOpinion(opinionId).toDto()

    fun getOpinionFor(subjectId: UUID): OpinionDto = opinionService.getOpinionFor(subjectId).toDto()

    fun createOpinion(
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): OpinionDto = opinionService.createOpinion(subjectId, subjective, objective, mark).toDto()

    fun updateOpinion(
        opinionId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): OpinionDto = opinionService.updateOpinion(opinionId, subjective, objective, mark).toDto()

    fun deleteOpinion(opinionId: UUID) {
        opinionService.deleteOpinion(opinionId)
    }

    fun linkComponent(
        parentOpinionId: UUID,
        childOpinionId: UUID,
        weight: Double,
    ): OpinionDto = opinionService.linkComponent(parentOpinionId, childOpinionId, weight).toDto()

    fun unlinkComponent(linkId: UUID): OpinionDto = opinionService.unlinkComponent(linkId).toDto()

    fun adjustComponentWeight(
        linkId: UUID,
        weight: Double,
    ): OpinionDto = opinionService.adjustComponentWeight(linkId, weight).toDto()

    private fun Opinion.toDto() =
        OpinionDto(
            id,
            owner,
            usersInternalApi.getUserName(owner),
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

    private companion object {
        fun OpinionStatusEnum.toDto(): OpinionStatusEnumDto =
            when (this) {
                OpinionStatusEnum.DRAFT -> OpinionStatusEnumDto.DRAFT
                OpinionStatusEnum.PENDING_PREMODERATION -> OpinionStatusEnumDto.PENDING_PREMODERATION
                OpinionStatusEnum.PUBLISHED -> OpinionStatusEnumDto.PUBLISHED
                OpinionStatusEnum.REJECTED -> OpinionStatusEnumDto.REJECTED
            }

        fun OpinionStatusEnumDto.fromDto(): OpinionStatusEnum =
            when (this) {
                OpinionStatusEnumDto.DRAFT -> OpinionStatusEnum.DRAFT
                OpinionStatusEnumDto.PENDING_PREMODERATION -> OpinionStatusEnum.PENDING_PREMODERATION
                OpinionStatusEnumDto.PUBLISHED -> OpinionStatusEnum.PUBLISHED
                OpinionStatusEnumDto.REJECTED -> OpinionStatusEnum.REJECTED
            }

        fun WeightedOpinionReference.toDto() =
            WeightedOpinionReferenceDto(
                id,
                opinion,
                weight,
            )

        fun WeightedOpinionReferenceDto.fromDto() =
            WeightedOpinionReference(
                id,
                opinion,
                weight,
            )
    }
}