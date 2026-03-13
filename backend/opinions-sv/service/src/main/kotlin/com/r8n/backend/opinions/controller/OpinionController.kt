package com.r8n.backend.opinions.controller

import com.r8n.backend.mock.stub.OpinionTestDataFactory
import com.r8n.backend.opinions.api.OpinionApi
import com.r8n.backend.opinions.api.dto.OpinionDto
import com.r8n.backend.opinions.facade.OpinionFacade
import jakarta.websocket.server.PathParam
import org.springframework.web.bind.annotation.DeleteMapping
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController
class OpinionController(
    private val opinionFacade: OpinionFacade,
) : OpinionApi {

    override fun getOpinionById(
        @PathVariable
        id: UUID,
    ) = opinionFacade.getOpinionDto(id)

    override fun getOpinionFor(
        @PathVariable
        subjectId: UUID,
    ) = OpinionTestDataFactory.getOpinion(subjectId)

    override fun createOpinion(
        @RequestParam(required = true)
        subjectId: UUID,
        @RequestParam(required = false)
        subjective: List<String>,
        @RequestParam(required = false)
        objective: List<String>,
        @RequestParam(required = false)
        mark: Double?,
    ) = OpinionTestDataFactory.postOpinion(
        subjectId = subjectId,
        subjective = subjective,
        objective = objective,
        mark = mark,
    )

    override fun updateOpinion(
        @PathVariable
        opinionId: UUID,
        @RequestParam(required = false)
        subjective: List<String>,
        @RequestParam(required = false)
        objective: List<String>,
        @RequestParam(required = false)
        mark: Double?,
    ) = OpinionTestDataFactory.postOpinion(
        opinionId = opinionId,
        subjective = subjective,
        objective = objective,
        mark = mark,
    )

    override fun deleteOpinion(
        @PathVariable
        opinionId: UUID,
    ) {
    }

    override fun linkComponent(
        @RequestParam(required = true)
        parentOpinionId: UUID,
        @RequestParam(required = true)
        childOpinionId: UUID,
        @RequestParam(required = true)
        weight: Double,
    ) = OpinionTestDataFactory.getOpinion(parentOpinionId)

    override fun unlinkComponent(
        @PathVariable
        linkId: UUID,
    ): OpinionDto =
        OpinionTestDataFactory.getOpinion(UUID.fromString("0"))

    override fun adjustComponentWeight(
        @PathVariable
        linkId: UUID,
        @RequestParam(required = true)
        weight: Double,
    ): OpinionDto =
        OpinionTestDataFactory.getOpinion(UUID.fromString("0"))

}