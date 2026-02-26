package com.r8n.backend.opinions.controller

import com.r8n.backend.opinions.api.OpinionApi
import com.r8n.backend.opinions.api.dto.opinion.OpinionDto
import com.r8n.backend.opinions.stub.OpinionTestDataFactory
import org.springframework.web.bind.annotation.DeleteMapping
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/opinions")
class StubOpinionController : OpinionApi {

    @GetMapping("/id")
    override fun getOpinionById(
        @RequestParam(required = true)
        id: UUID,
    ) = OpinionTestDataFactory.getOpinion(id)

    @GetMapping("/for")
    override fun getOpinionFor(
        @RequestParam(required = true)
        subjectId: UUID,
    ) = OpinionTestDataFactory.getOpinion(subjectId)

    @PostMapping("/add")
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

    @PatchMapping("/update")
    override fun updateOpinion(
        @RequestParam(required = true)
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

    @DeleteMapping("/delete")
    override fun deleteOpinion(
        @RequestParam(required = true)
        opinionId: UUID,
    ) {
    }

    @PostMapping("/link")
    override fun linkComponent(
        @RequestParam(required = true)
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