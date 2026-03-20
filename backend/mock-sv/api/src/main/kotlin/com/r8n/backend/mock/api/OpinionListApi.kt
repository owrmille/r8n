package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.list.OpinionListDto
import com.r8n.backend.mock.api.dto.list.OpinionListPrivacyEnumDto
import com.r8n.backend.mock.api.dto.list.OpinionListSummaryDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

interface OpinionListApi {
    companion object {
        const val SUMMARY_PATH = "/opinion-lists/{listId}/summary"
        const val GET_PATH = "/opinion-lists/{listId}"
        const val RENAME_PATH = "/opinion-lists/{listId}/rename"
        const val SET_PRIVACY_PATH = "/opinion-lists/{listId}/set-privacy"
        const val LINK_PATH = "/opinion-lists/{listId}/link"
        const val UNLINK_PATH = "/opinion-lists/{listId}/unlink"
        const val SEARCH_PATH = "/opinion-lists/search"
        const val SYNC_PATH = "/opinion-lists/{existingListId}/sync"
        const val UNSYNC_PATH = "/opinion-lists/{existingListId}/unsync"
        const val MINE_PATH = "/opinion-lists/mine"
        const val MINE_FULL_PATH = "/opinion-lists/mine/full"
    }
    @GetMapping(SUMMARY_PATH)
    fun getListSummary(@PathVariable listId: UUID): OpinionListSummaryDto

    @GetMapping(GET_PATH)
    fun getList(@PathVariable listId: UUID): OpinionListDto

    @PatchMapping(RENAME_PATH)
    fun renameList(
        @PathVariable
        listId: UUID,
        @RequestParam(required = true)
        name: String,
    ): OpinionListDto

    @PatchMapping(SET_PRIVACY_PATH)
    fun changePrivacy(
        @PathVariable
        listId: UUID,
        @RequestParam(required = true)
        privacy: OpinionListPrivacyEnumDto,
    ): OpinionListDto

    @PostMapping(LINK_PATH)
    fun linkOpinion(
        @PathVariable
        listId: UUID,
        @RequestParam(required = true)
        opinionId: UUID,
    ): OpinionListDto

    @PatchMapping(UNLINK_PATH)
    fun unlinkOpinion(
        @PathVariable
        listId: UUID,
        @RequestParam(required = true)
        opinionId: UUID,
    ): OpinionListDto

    @GetMapping(SEARCH_PATH)
    fun search(
        @RequestParam(required = false)
        nameSubstring: String?,
        @RequestParam(required = false)
        authorId: UUID?,
        @RequestParam(required = false)
        authorNameSubstring: String?,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionListSummaryDto>

    @PostMapping(SYNC_PATH)
    fun syncWithOpinionList(
        @PathVariable
        existingListId: UUID,
        @RequestParam(required = true)
        addedListId: UUID,
    ): OpinionListDto

    @PostMapping(UNSYNC_PATH)
    fun unsyncWithOpinionList(
        @PathVariable
        existingListId: UUID,
        @RequestParam(required = true)
        removedListId: UUID,
    ): OpinionListDto

    @GetMapping(MINE_PATH)
    fun getMine(pageable: PageRequestDto): PageResponseDto<OpinionListSummaryDto>
}
