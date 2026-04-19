package com.r8n.backend.opinions.api

import com.r8n.backend.opinions.api.dto.OpinionDto
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

interface OpinionApi {
    companion object {
        private const val ROOT_PATH = "/api/opinions"
        const val GET_BY_ID_PATH = "$ROOT_PATH/{id}"
        const val GET_FOR_SUBJECT_PATH = "$ROOT_PATH/for/{subjectId}"
        const val CREATE_PATH = ROOT_PATH
        const val UPDATE_PATH = "$ROOT_PATH/{opinionId}"
        const val DELETE_PATH = "$ROOT_PATH/{opinionId}"
        const val LINK_PATH = "$ROOT_PATH/link"
        const val UNLINK_PATH = "$ROOT_PATH/unlink/{linkId}"
        const val ADJUST_WEIGHT_PATH = "$ROOT_PATH/adjust-weight/{linkId}"
    }

    @GetMapping(GET_BY_ID_PATH)
    fun getOpinionById(
        @PathVariable id: UUID,
    ): OpinionDto

    @GetMapping(GET_FOR_SUBJECT_PATH)
    fun getOpinionFor(
        @PathVariable subjectId: UUID,
    ): OpinionDto

    @PostMapping(CREATE_PATH)
    fun createOpinion(
        @RequestParam(required = true)
        subjectId: UUID,
        @RequestParam(required = false)
        subjective: List<String>,
        @RequestParam(required = false)
        objective: List<String>,
        @RequestParam(required = false)
        mark: Double?,
    ): OpinionDto

    @PatchMapping(UPDATE_PATH)
    fun updateOpinion(
        @PathVariable opinionId: UUID,
        @RequestParam(required = false)
        subjective: List<String>,
        @RequestParam(required = false)
        objective: List<String>,
        @RequestParam(required = false)
        mark: Double?,
    ): OpinionDto

    @DeleteMapping(DELETE_PATH)
    fun deleteOpinion(
        @PathVariable opinionId: UUID,
    )

    @PostMapping(LINK_PATH)
    fun linkComponent(
        @RequestParam(required = true)
        parentOpinionId: UUID,
        @RequestParam(required = true)
        childOpinionId: UUID,
        @RequestParam(required = true)
        weight: Double,
    ): OpinionDto

    @DeleteMapping(UNLINK_PATH)
    fun unlinkComponent(
        @PathVariable linkId: UUID,
    ): OpinionDto

    @PatchMapping(ADJUST_WEIGHT_PATH)
    fun adjustComponentWeight(
        @PathVariable linkId: UUID,
        @RequestParam(required = true)
        weight: Double,
    ): OpinionDto
}