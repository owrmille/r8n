package com.r8n.backend.opinions.integration.client

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import com.r8n.backend.opinions.integration.api.OpinionsInternalApi
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class OpinionsInternalRestClient(
    private val restClient: RestClient,
) : OpinionsInternalApi {
    override fun getMyFullOpinions(pageable: PageRequestDto): PageResponseDto<OpinionDto> =
        restClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path(OpinionsInternalApi.MINE_FULL_PATH)
                    .queryParam("page", pageable.page)
                    .queryParam("size", pageable.size)
                    .apply {
                        pageable.sort.forEach {
                            queryParam("sort", "${it.property},${it.direction}")
                        }
                    }.build()
            }.retrieve()
            .body<PageResponseDto<OpinionDto>>()!!

    override fun restoreOpinion(opinion: OpinionDto) {
        restClient
            .post()
            .uri(OpinionsInternalApi.RESTORE_PATH)
            .body(opinion)
            .retrieve()
            .toBodilessEntity()
    }
}
