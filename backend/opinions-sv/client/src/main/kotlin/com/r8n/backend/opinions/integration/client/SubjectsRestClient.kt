package com.r8n.backend.opinions.integration.client

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionSubjectDto
import com.r8n.backend.opinions.api.subjects.SubjectsApi
import com.r8n.backend.opinions.api.subjects.SubjectsApi.Companion.CREATE_PATH
import com.r8n.backend.opinions.api.subjects.SubjectsApi.Companion.FIND_PATH
import com.r8n.backend.opinions.api.subjects.dto.CreateSubjectRequestDto
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class SubjectsRestClient(
    private val restClient: RestClient,
) : SubjectsApi {
    override fun findSubject(
        query: String,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionSubjectDto> =
        restClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path(FIND_PATH)
                    .queryParam("query", query)
                    .queryParam("page", pageable.page)
                    .queryParam("size", pageable.size)
                    .apply {
                        pageable.sort.forEach { sort ->
                            queryParam("sort", "${sort.property},${sort.direction}")
                        }
                    }.build()
            }.retrieve()
            .body<PageResponseDto<OpinionSubjectDto>>()!!

    override fun createSubject(request: CreateSubjectRequestDto): OpinionSubjectDto =
        restClient
            .post()
            .uri(CREATE_PATH)
            .body(request)
            .retrieve()
            .body<OpinionSubjectDto>()!!
}
