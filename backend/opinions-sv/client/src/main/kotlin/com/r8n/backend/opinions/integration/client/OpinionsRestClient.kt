package com.r8n.backend.opinions.integration.client

import com.r8n.backend.opinions.api.opinions.OpinionsApi
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.ADJUST_WEIGHT_PATH
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.CREATE_PATH
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.DELETE_PATH
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.GET_BY_ID_PATH
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.GET_FOR_SUBJECT_PATH
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.LINK_PATH
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.SUBMIT_FOR_MODERATION_PATH
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.UNLINK_PATH
import com.r8n.backend.opinions.api.opinions.OpinionsApi.Companion.UPDATE_PATH
import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.UUID

class OpinionsRestClient(
    private val restClient: RestClient,
) : OpinionsApi {
    override fun getOpinionById(id: UUID): OpinionDto =
        restClient
            .get()
            .uri(GET_BY_ID_PATH, id)
            .retrieve()
            .body<OpinionDto>()!!

    override fun getOpinionFor(subjectId: UUID): OpinionDto =
        restClient
            .get()
            .uri(GET_FOR_SUBJECT_PATH, subjectId)
            .retrieve()
            .body<OpinionDto>()!!

    override fun createOpinion(
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): OpinionDto =
        restClient
            .post()
            .uri { uriBuilder ->
                uriBuilder
                    .path(CREATE_PATH)
                    .queryParam("subjectId", subjectId)
                    .apply {
                        if (subjective.isNotEmpty()) queryParam("subjective", *subjective.toTypedArray())
                        if (objective.isNotEmpty()) queryParam("objective", *objective.toTypedArray())
                        mark?.let { queryParam("mark", it) }
                    }.build()
            }.retrieve()
            .body<OpinionDto>()!!

    override fun updateOpinion(
        opinionId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): OpinionDto =
        restClient
            .patch()
            .uri { uriBuilder ->
                uriBuilder
                    .path(UPDATE_PATH)
                    .apply {
                        if (subjective.isNotEmpty()) queryParam("subjective", *subjective.toTypedArray())
                        if (objective.isNotEmpty()) queryParam("objective", *objective.toTypedArray())
                        mark?.let { queryParam("mark", it) }
                    }.build(opinionId)
            }.retrieve()
            .body<OpinionDto>()!!

    override fun deleteOpinion(opinionId: UUID) {
        restClient
            .delete()
            .uri(DELETE_PATH, opinionId)
            .retrieve()
            .toBodilessEntity()
    }

    override fun submitOpinionForModeration(opinionId: UUID): OpinionDto =
        restClient
            .post()
            .uri(SUBMIT_FOR_MODERATION_PATH, opinionId)
            .retrieve()
            .body<OpinionDto>()!!

    override fun linkComponent(
        parentOpinionId: UUID,
        childOpinionId: UUID,
        weight: Double,
    ): OpinionDto =
        restClient
            .post()
            .uri { uriBuilder ->
                uriBuilder
                    .path(LINK_PATH)
                    .queryParam("parentOpinionId", parentOpinionId)
                    .queryParam("childOpinionId", childOpinionId)
                    .queryParam("weight", weight)
                    .build()
            }.retrieve()
            .body<OpinionDto>()!!

    override fun unlinkComponent(linkId: UUID): OpinionDto =
        restClient
            .delete()
            .uri(UNLINK_PATH, linkId)
            .retrieve()
            .body<OpinionDto>()!!

    override fun adjustComponentWeight(
        linkId: UUID,
        weight: Double,
    ): OpinionDto =
        restClient
            .patch()
            .uri { uriBuilder ->
                uriBuilder
                    .path(ADJUST_WEIGHT_PATH)
                    .queryParam("weight", weight)
                    .build(linkId)
            }.retrieve()
            .body<OpinionDto>()!!
}
