package com.r8n.backend.opinionlists.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.stub.OpinionListTestDataFactory
import com.r8n.backend.opinionlists.api.OpinionListsApi
import com.r8n.backend.opinionlists.api.dto.OpinionListPrivacyEnumDto
import com.r8n.backend.opinionlists.api.dto.OpinionListSummaryDto
import com.r8n.backend.opinionlists.facade.OpinionListFacade
import com.r8n.backend.security.Authority
import org.springframework.data.domain.PageImpl
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class StubOpinionListController(
    private val opinionListFacade: OpinionListFacade,
) : OpinionListsApi {
    @PreAuthorize(Authority.IS_USER)
    override fun getListSummary(listId: UUID): OpinionListSummaryDto = OpinionListTestDataFactory.getListSummary(listId)

    @PreAuthorize(Authority.IS_USER)
    override fun getList(listId: UUID) = opinionListFacade.getList(listId)

    @PreAuthorize(Authority.IS_USER)
    override fun renameList(
        listId: UUID,
        name: String,
    ) = OpinionListTestDataFactory.getList()

    @PreAuthorize(Authority.IS_USER)
    override fun changePrivacy(
        listId: UUID,
        privacy: OpinionListPrivacyEnumDto,
    ) = OpinionListTestDataFactory.getList(listId)

    @PreAuthorize(Authority.IS_USER)
    override fun linkOpinion(
        listId: UUID,
        opinionId: UUID,
    ) = OpinionListTestDataFactory.getList(listId)

    @PreAuthorize(Authority.IS_USER)
    override fun unlinkOpinion(
        listId: UUID,
        opinionId: UUID,
    ) = OpinionListTestDataFactory.getList(listId)

    @PreAuthorize(Authority.IS_USER)
    override fun search(
        nameSubstring: String?,
        authorId: UUID?,
        authorNameSubstring: String?,
        pageable: PageRequestDto,
    ) = PageImpl(
        listOf(OpinionListTestDataFactory.getListSummary()),
    ).toResponse()

    @PreAuthorize(Authority.IS_USER)
    override fun syncWithOpinionList(
        existingListId: UUID,
        addedListId: UUID,
    ) = OpinionListTestDataFactory.getList(existingListId)

    @PreAuthorize(Authority.IS_USER)
    override fun unsyncWithOpinionList(
        existingListId: UUID,
        removedListId: UUID,
    ) = OpinionListTestDataFactory.getList(existingListId)

    @PreAuthorize(Authority.IS_USER)
    override fun getMine(pageable: PageRequestDto) = search(null, null, null, pageable)
}