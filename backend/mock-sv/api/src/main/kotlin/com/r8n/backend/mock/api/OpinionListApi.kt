package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.list.OpinionListDto
import com.r8n.backend.mock.api.dto.list.OpinionListPrivacyEnumDto
import com.r8n.backend.mock.api.dto.list.OpinionListSummaryDto
import java.util.UUID

interface OpinionListApi {
    fun getListSummary(listId: UUID): OpinionListSummaryDto
    fun getList(listId: UUID): OpinionListDto
    fun renameList(listId: UUID, name: String): OpinionListDto
    fun changePrivacy(listId: UUID, privacy: OpinionListPrivacyEnumDto): OpinionListDto
    fun linkOpinion(listId: UUID, opinionId: UUID): OpinionListDto
    fun unlinkOpinion(listId: UUID, opinionId: UUID): OpinionListDto
    fun search(
        nameSubstring: String?,
        authorId: UUID?,
        authorNameSubstring: String?,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionListSummaryDto>
    fun syncWithOpinionList(existingList: UUID, addedList: UUID): OpinionListDto
    fun unsyncWithOpinionList(existingList: UUID, removedList: UUID): OpinionListDto
    fun getMine(pageable: PageRequestDto): PageResponseDto<OpinionListSummaryDto>
}
