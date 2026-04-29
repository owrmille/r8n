package com.r8n.backend.opinions.lists.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.opinions.access.database.AccessRequestRepository
import com.r8n.backend.opinions.access.domain.RequestStatusEnum
import com.r8n.backend.opinions.api.lists.OpinionListsApi
import com.r8n.backend.opinions.api.lists.dto.OpinionListPrivacyEnumDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListSummaryDto
import com.r8n.backend.opinions.lists.facade.OpinionListFacade
import com.r8n.backend.opinions.stub.OpinionListTestDataFactory
import com.r8n.backend.security.Authority.IS_USER
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import org.springframework.data.domain.PageImpl
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class StubOpinionListController(
    private val opinionListFacade: OpinionListFacade,
    private val accessRequestRepository: AccessRequestRepository,
) : OpinionListsApi {
    @PreAuthorize(IS_USER)
    override fun getListSummary(listId: UUID): OpinionListSummaryDto = OpinionListTestDataFactory.getListSummary(listId)

    @PreAuthorize(IS_USER)
    override fun getList(listId: UUID) = opinionListFacade.getList(listId, getCurrentUserId())

    @PreAuthorize(IS_USER)
    override fun renameList(
        listId: UUID,
        name: String,
    ) = OpinionListTestDataFactory.getList()

    @PreAuthorize(IS_USER)
    override fun changePrivacy(
        listId: UUID,
        privacy: OpinionListPrivacyEnumDto,
    ) = OpinionListTestDataFactory.getList(listId)

    @PreAuthorize(IS_USER)
    override fun linkOpinion(
        listId: UUID,
        opinionId: UUID,
        weight: Double,
    ) = opinionListFacade.linkOpinion(getCurrentUserId(), listId, opinionId, weight)

    @PreAuthorize(IS_USER)
    override fun unlinkOpinion(
        listId: UUID,
        opinionId: UUID,
    ) = opinionListFacade.unlinkOpinion(getCurrentUserId(), listId, opinionId)

    @PreAuthorize(IS_USER)
    override fun search(
        nameSubstring: String?,
        authorId: UUID?,
        authorNameSubstring: String?,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionListSummaryDto> {
        // Only implement search by name substring as per scope
        if (!nameSubstring.isNullOrBlank()) {
            return opinionListFacade.searchOpinionListsByName(nameSubstring, pageable)
        }
        // Return empty page if no search criteria provided
        return PageImpl<OpinionListSummaryDto>(emptyList()).toResponse()
    }

    @PreAuthorize(IS_USER)
    override fun syncWithOpinionList(
        existingListId: UUID,
        addedListId: UUID,
        weight: Double,
    ) = opinionListFacade.syncWithOpinionList(getCurrentUserId(), existingListId, addedListId, weight)

    @PreAuthorize(IS_USER)
    override fun unsyncWithOpinionList(
        existingListId: UUID,
        removedListId: UUID,
    ) = opinionListFacade.unsyncWithOpinionList(getCurrentUserId(), existingListId, removedListId)

    @PreAuthorize(IS_USER)
    override fun getMine(pageable: PageRequestDto): com.r8n.backend.core.api.PageResponseDto<OpinionListSummaryDto> {
        val fullLists = opinionListFacade.getListsFull(getCurrentUserId(), pageable)
        // Convert full DTOs to summary DTOs
        return com.r8n.backend.core.api.PageResponseDto(
            items =
                fullLists.items.map { full ->
                    val grantedAccessCount =
                        accessRequestRepository.countByListAndStatus(
                            full.id,
                            RequestStatusEnum.ACCEPTED,
                        )
                    OpinionListSummaryDto(
                        listId = full.id,
                        listName = full.listName,
                        owner = full.owner,
                        ownerName = full.ownerName,
                        opinionsCount = full.opinionSummaries.size.toLong(),
                        grantedAccessCount = grantedAccessCount.toInt(),
                        privacy = full.privacy,
                    )
                },
            total = fullLists.total,
            page = fullLists.page,
            size = fullLists.size,
        )
    }
}
