package com.r8n.backend.opinions.controller

import com.r8n.backend.mock.stub.OpinionTestDataFactory
import com.r8n.backend.opinions.api.OpinionApi
import com.r8n.backend.opinions.api.dto.opinion.OpinionDto
import com.r8n.backend.opinions.facade.OpinionFacade
import org.springframework.web.bind.annotation.DeleteMapping
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController
class StubOpinionController(
    private val opinionFacade: OpinionFacade,
) : OpinionApi {

    override fun getOpinionById(
        id: UUID,
    ) = opinionFacade.getOpinionDto(id)

    override fun getOpinionFor(
        subjectId: UUID,
    ) = OpinionTestDataFactory.getOpinion(subjectId)

    override fun createOpinion(
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ) = OpinionTestDataFactory.postOpinion(
        subjectId = subjectId,
        subjective = subjective,
        objective = objective,
        mark = mark,
    )

    override fun updateOpinion(
        opinionId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ) = OpinionTestDataFactory.postOpinion(
        opinionId = opinionId,
        subjective = subjective,
        objective = objective,
        mark = mark,
    )

    override fun deleteOpinion(
        opinionId: UUID,
    ) {
    }

    override fun linkComponent(
        parentOpinionId: UUID,
        @RequestParam(required = true)
        childOpinionId: UUID,
        @RequestParam(required = true)
        weight: Double,
    ) = OpinionTestDataFactory.getOpinion(parentOpinionId)

    @DeleteMapping("/unlink")
    override fun unlinkComponent(
        @RequestParam(required = true)
        linkId: UUID,
    ): OpinionDto =
        OpinionTestDataFactory.getOpinion(UUID.fromString("0"))

    @PatchMapping("/adjustComponentWeight")
    override fun adjustComponentWeight(
        @RequestParam(required = true)
        linkId: UUID,
        @RequestParam(required = true)
        weight: Double,
    ): OpinionDto =
        OpinionTestDataFactory.getOpinion(UUID.fromString("0"))

}