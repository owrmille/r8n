package com.r8n.backend.mock.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.api.OpinionListApi
import com.r8n.backend.mock.api.dto.list.OpinionListPrivacyEnumDto
import com.r8n.backend.mock.api.dto.list.OpinionListSummaryDto
import com.r8n.backend.mock.stub.OpinionListTestDataFactory
import org.springframework.data.domain.PageImpl
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class StubOpinionListController : OpinionListApi {
    override fun getListSummary(listId: UUID): OpinionListSummaryDto = OpinionListTestDataFactory.getListSummary(listId)

    override fun getList(listId: UUID) = OpinionListTestDataFactory.getList(listId)

    override fun renameList(
        listId: UUID,
        name: String,
    ) = OpinionListTestDataFactory.getList()

    override fun changePrivacy(
        listId: UUID,
        privacy: OpinionListPrivacyEnumDto,
    ) = OpinionListTestDataFactory.getList(listId)

    override fun linkOpinion(
        listId: UUID,
        opinionId: UUID,
    ) = OpinionListTestDataFactory.getList(listId)

    override fun unlinkOpinion(
        listId: UUID,
        opinionId: UUID,
    ) = OpinionListTestDataFactory.getList(listId)

    override fun search(
        nameSubstring: String?,
        authorId: UUID?,
        authorNameSubstring: String?,
        pageable: PageRequestDto,
    ) = PageImpl(
        listOf(OpinionListTestDataFactory.getListSummary()),
    ).toResponse()

    override fun syncWithOpinionList(
        existingListId: UUID,
        addedListId: UUID,
    ) = OpinionListTestDataFactory.getList(existingListId)

    override fun unsyncWithOpinionList(
        existingListId: UUID,
        removedListId: UUID,
    ) = OpinionListTestDataFactory.getList(existingListId)

    override fun getMine(pageable: PageRequestDto) = search(null, null, null, pageable)
}