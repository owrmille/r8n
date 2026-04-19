package com.r8n.backend.opinionlists.integration.client

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinionlists.api.OpinionListsApi
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.GET_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.LINK_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.MINE_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.RENAME_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.SEARCH_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.SET_PRIVACY_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.SUMMARY_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.SYNC_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.UNLINK_PATH
import com.r8n.backend.opinionlists.api.OpinionListsApi.Companion.UNSYNC_PATH
import com.r8n.backend.opinionlists.api.dto.OpinionListDto
import com.r8n.backend.opinionlists.api.dto.OpinionListPrivacyEnumDto
import com.r8n.backend.opinionlists.api.dto.OpinionListSummaryDto
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.Optional
import java.util.UUID

class OpinionListRestClient(
    private val restClient: RestClient,
) : OpinionListsApi {
    override fun getListSummary(listId: UUID): OpinionListSummaryDto =
        restClient
            .get()
            .uri(SUMMARY_PATH, listId)
            .retrieve()
            .body<OpinionListSummaryDto>()!!

    override fun getList(listId: UUID): OpinionListDto =
        restClient
            .get()
            .uri(GET_PATH, listId)
            .retrieve()
            .body<OpinionListDto>()!!

    override fun renameList(
        listId: UUID,
        name: String,
    ): OpinionListDto =
        restClient
            .patch()
            .uri { uriBuilder ->
                uriBuilder.path(RENAME_PATH).queryParam("name", name).build(listId)
            }.retrieve()
            .body<OpinionListDto>()!!

    override fun changePrivacy(
        listId: UUID,
        privacy: OpinionListPrivacyEnumDto,
    ): OpinionListDto =
        restClient
            .patch()
            .uri { uriBuilder ->
                uriBuilder.path(SET_PRIVACY_PATH).queryParam("privacy", privacy).build(listId)
            }.retrieve()
            .body<OpinionListDto>()!!

    override fun linkOpinion(
        listId: UUID,
        opinionId: UUID,
    ): OpinionListDto =
        restClient
            .post()
            .uri { uriBuilder ->
                uriBuilder.path(LINK_PATH).queryParam("opinionId", opinionId).build(listId)
            }.retrieve()
            .body<OpinionListDto>()!!

    override fun unlinkOpinion(
        listId: UUID,
        opinionId: UUID,
    ): OpinionListDto =
        restClient
            .patch()
            .uri { uriBuilder ->
                uriBuilder.path(UNLINK_PATH).queryParam("opinionId", opinionId).build(listId)
            }.retrieve()
            .body<OpinionListDto>()!!

    override fun search(
        nameSubstring: String?,
        authorId: UUID?,
        authorNameSubstring: String?,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionListSummaryDto> =
        restClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path(SEARCH_PATH)
                    .queryParamIfPresent("nameSubstring", Optional.ofNullable(nameSubstring))
                    .queryParamIfPresent("authorId", Optional.ofNullable(authorId))
                    .queryParamIfPresent("authorNameSubstring", Optional.ofNullable(authorNameSubstring))
                    .queryParam("page", pageable.page)
                    .queryParam("size", pageable.size)
                    .apply {
                        pageable.sort.forEach {
                            queryParam("sort", "${it.property},${it.direction}")
                        }
                    }.build()
            }.retrieve()
            .body<PageResponseDto<OpinionListSummaryDto>>()!!

    override fun syncWithOpinionList(
        existingListId: UUID,
        addedListId: UUID,
    ): OpinionListDto =
        restClient
            .post()
            .uri { uriBuilder ->
                uriBuilder.path(SYNC_PATH).queryParam("addedListId", addedListId).build(existingListId)
            }.retrieve()
            .body<OpinionListDto>()!!

    override fun unsyncWithOpinionList(
        existingListId: UUID,
        removedListId: UUID,
    ): OpinionListDto =
        restClient
            .post()
            .uri { uriBuilder ->
                uriBuilder.path(UNSYNC_PATH).queryParam("removedListId", removedListId).build(existingListId)
            }.retrieve()
            .body<OpinionListDto>()!!

    override fun getMine(pageable: PageRequestDto): PageResponseDto<OpinionListSummaryDto> =
        restClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path(MINE_PATH)
                    .queryParam("page", pageable.page)
                    .queryParam("size", pageable.size)
                    .apply {
                        pageable.sort.forEach {
                            queryParam("sort", "${it.property},${it.direction}")
                        }
                    }.build()
            }.retrieve()
            .body<PageResponseDto<OpinionListSummaryDto>>()!!
}