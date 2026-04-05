package com.r8n.backend.mock.integration.client

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.list.OpinionListDto
import com.r8n.backend.mock.integration.api.OpinionListInternalApi
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class OpinionListInternalRestClient(
    private val restClient: RestClient,
) : OpinionListInternalApi {
    override fun getMineFull(pageable: PageRequestDto): PageResponseDto<OpinionListDto> =
        restClient.get().uri { uriBuilder ->
            uriBuilder.path(OpinionListInternalApi.MINE_FULL_PATH)
                .queryParam("page", pageable.page)
                .queryParam("size", pageable.size)
                .apply {
                    pageable.sort.forEach {
                        queryParam("sort", "${it.property},${it.direction}")
                    }
                }
                .build()
        }.retrieve().body<PageResponseDto<OpinionListDto>>()!!
}
