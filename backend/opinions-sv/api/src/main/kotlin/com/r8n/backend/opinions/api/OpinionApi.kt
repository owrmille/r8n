package com.r8n.backend.opinions.api

import com.r8n.backend.opinions.api.dto.OpinionDto
import java.util.UUID

interface OpinionApi {
    fun getOpinionById(id: UUID): OpinionDto
    fun getOpinionFor(subjectId: UUID): OpinionDto
    fun createOpinion(subjectId: UUID, subjective: List<String>, objective: List<String>, mark: Double?): OpinionDto
    fun updateOpinion(opinionId: UUID, subjective: List<String>, objective: List<String>, mark: Double?): OpinionDto
    fun deleteOpinion(opinionId: UUID)
    fun linkComponent(parentOpinionId: UUID, childOpinionId: UUID, weight: Double): OpinionDto
    fun unlinkComponent(linkId: UUID): OpinionDto
    fun adjustComponentWeight(linkId: UUID, weight: Double): OpinionDto
}