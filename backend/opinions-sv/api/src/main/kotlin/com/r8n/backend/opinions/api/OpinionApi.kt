package com.r8n.backend.opinions.api

import com.r8n.backend.opinions.api.dto.opinion.OpinionDto
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import java.util.UUID

interface OpinionApi {
    @GetMapping("/opinions/{id}")
    fun getOpinionById(@PathVariable id: UUID): OpinionDto

    @GetMapping("/opinions/for/{subjectId}")
    fun getOpinionFor(@PathVariable subjectId: UUID): OpinionDto

    @PostMapping("/opinions")
    fun createOpinion(subjectId: UUID, subjective: List<String>, objective: List<String>, mark: Double?): OpinionDto

    @PatchMapping("/opinions/{opinionId}")
    fun updateOpinion(@PathVariable opinionId: UUID, subjective: List<String>, objective: List<String>, mark: Double?): OpinionDto

    @DeleteMapping("/opinions/{opinionId}")
    fun deleteOpinion(@PathVariable opinionId: UUID)

    @PostMapping("/opinions/link")
    fun linkComponent(parentOpinionId: UUID, childOpinionId: UUID, weight: Double): OpinionDto

    @DeleteMapping("/opinions/unlink/{linkId}")
    fun unlinkComponent(@PathVariable linkId: UUID): OpinionDto

    @PatchMapping("/opinions/adjustWeight/{linkId}")
    fun adjustComponentWeight(@PathVariable linkId: UUID, weight: Double): OpinionDto
}