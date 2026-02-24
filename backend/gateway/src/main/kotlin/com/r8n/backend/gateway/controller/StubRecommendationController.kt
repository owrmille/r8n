package com.r8n.backend.gateway.controller

import com.r8n.backend.gateway.api.RecommendationApi
import com.r8n.backend.gateway.api.dto.toResponse
import com.r8n.backend.gateway.stub.OpinionListTestDataFactory.getListSummary
import com.r8n.backend.gateway.stub.OpinionSubjectTestDataFactory.cappuccino2
import com.r8n.backend.gateway.stub.OpinionSubjectTestDataFactory.cappuccino3
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/recommendations")
class StubRecommendationController : RecommendationApi {
    @GetMapping("/subjects")
    override fun getRecommendedSubjects(
        @RequestParam(required = true)
        lookingAtSubjectId: UUID,
        pageable: Pageable,
    ) = PageImpl(
        listOf(
            cappuccino2,
            cappuccino3,
        ),
    ).toResponse()

    @GetMapping("/opinionLists")
    override fun getRecommendedOpinionLists(
        @RequestParam(required = true)
        lookingAtListId: UUID,
        pageable: Pageable,
    ) = PageImpl(
        listOf(getListSummary()),
    ).toResponse()

    @PatchMapping("/hide/subject")
    override fun hideRecommendedSubject(id: UUID) = Unit

    @PatchMapping("/hide/opinionList")
    override fun hideRecommendedOpinionList(id: UUID) = Unit

    @GetMapping("/hidden/subjects")
    override fun getHiddenSubjects(pageable: Pageable) = PageImpl(listOf(cappuccino3)).toResponse()

    @GetMapping("/hidden/opinionList")
    override fun getHiddenOpinionLists(pageable: Pageable) = PageImpl(listOf(getListSummary())).toResponse()

    @PatchMapping("/unhide/subject")
    override fun unhideSubject(
        @RequestParam(required = true)
        id: UUID,
    ) = cappuccino3

    @PatchMapping("/unhide/opinionList")
    override fun unhideOpinionList(
        @RequestParam(required = true)
        id: UUID,
    ) = getListSummary(id)
}