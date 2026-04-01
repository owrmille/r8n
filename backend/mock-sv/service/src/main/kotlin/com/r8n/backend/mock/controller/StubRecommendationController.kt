package com.r8n.backend.mock.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.mock.api.RecommendationApi
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.stub.OpinionListTestDataFactory.getListSummary
import com.r8n.backend.mock.stub.OpinionSubjectTestDataFactory.cappuccino2
import com.r8n.backend.mock.stub.OpinionSubjectTestDataFactory.cappuccino3
import org.springframework.data.domain.PageImpl
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class StubRecommendationController : RecommendationApi {
    override fun getRecommendedSubjects(
        lookingAtSubjectId: UUID,
        pageable: PageRequestDto,
    ) = PageImpl(
        listOf(
            cappuccino2,
            cappuccino3,
        ),
    ).toResponse()

    override fun getRecommendedOpinionLists(
        lookingAtListId: UUID,
        pageable: PageRequestDto,
    ) = PageImpl(
        listOf(getListSummary()),
    ).toResponse()

    override fun hideRecommendedSubject(id: UUID) = Unit

    override fun hideRecommendedOpinionList(id: UUID) = Unit

    override fun getHiddenSubjects(pageable: PageRequestDto) = PageImpl(listOf(cappuccino3)).toResponse()

    override fun getHiddenOpinionLists(pageable: PageRequestDto) = PageImpl(listOf(getListSummary())).toResponse()

    override fun unhideSubject(
        id: UUID,
    ) = cappuccino3

    override fun unhideOpinionList(
        id: UUID,
    ) = getListSummary(id)
}