package com.r8n.backend.mock.controller

import com.r8n.backend.mock.api.OpinionListApi
import com.r8n.backend.mock.api.dto.list.OpinionListPrivacyEnumDto
import com.r8n.backend.mock.api.dto.list.OpinionListSummaryDto
import com.r8n.backend.opinions.api.dto.toResponse
import com.r8n.backend.mock.stub.OpinionListTestDataFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/opinionLists")
class StubOpinionListController : OpinionListApi {

    @GetMapping("/summary")
    override fun getListSummary(
        @RequestParam(required = true)
        listId: UUID,
    ): OpinionListSummaryDto = OpinionListTestDataFactory.getListSummary(listId)

    @GetMapping("/get")
    override fun getList(
        @RequestParam(required = true)
        listId: UUID,
    ) = OpinionListTestDataFactory.getList(listId)

    @PatchMapping("/rename")
    override fun renameList(
        @RequestParam(required = true)
        listId: UUID,
        @RequestParam(required = true)
        name: String,
    ) = OpinionListTestDataFactory.getList()

    @PatchMapping("/setPrivacy")
    override fun changePrivacy(
        @RequestParam(required = true)
        listId: UUID,
        @RequestParam(required = true)
        privacy: OpinionListPrivacyEnumDto,
    ) =
        OpinionListTestDataFactory.getList(listId)

    @PostMapping("/linkOpinion")
    override fun linkOpinion(
        @RequestParam(required = true)
        listId: UUID,
        @RequestParam(required = true)
        opinionId: UUID,
    ) = OpinionListTestDataFactory.getList(listId)

    @PatchMapping("/unlinkOpinion")
    override fun unlinkOpinion(
        @RequestParam(required = true)
        listId: UUID,
        @RequestParam(required = true)
        opinionId: UUID,
    ) = OpinionListTestDataFactory.getList(listId)

    @GetMapping("/search")
    override fun search(
        @RequestParam(required = false)
        nameSubstring: String?,
        @RequestParam(required = false)
        authorId: UUID?,
        @RequestParam(required = false)
        authorNameSubstring: String?,
        pageable: Pageable,
    ) = PageImpl(
        listOf(OpinionListTestDataFactory.getListSummary()),
    ).toResponse()

    @PostMapping("/sync")
    override fun syncWithOpinionList(
        @RequestParam(required = true)
        existingList: UUID,
        @RequestParam(required = true)
        addedList: UUID,
    ) = OpinionListTestDataFactory.getList(existingList)

    @PostMapping("/unsync")
    override fun unsyncWithOpinionList(
        @RequestParam(required = true)
        existingList: UUID,
        @RequestParam(required = true)
        removedList: UUID,
    ) = OpinionListTestDataFactory.getList(existingList)

    @GetMapping("/mine")
    override fun getMine(pageable: Pageable) = search(null, null, null, pageable)
}