package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.list.OpinionListSummaryDto
import com.r8n.backend.opinions.api.dto.about.OpinionSubjectDto
import java.util.UUID

interface RecommendationApi {
    fun getRecommendedSubjects(lookingAtSubjectId: UUID, pageable: PageRequestDto): PageResponseDto<OpinionSubjectDto>
    fun getRecommendedOpinionLists(lookingAtListId: UUID, pageable: PageRequestDto): PageResponseDto<OpinionListSummaryDto>
    fun hideRecommendedSubject(id: UUID)
    fun hideRecommendedOpinionList(id: UUID)
    fun getHiddenSubjects(pageable: PageRequestDto): PageResponseDto<OpinionSubjectDto>
    fun getHiddenOpinionLists(pageable: PageRequestDto): PageResponseDto<OpinionListSummaryDto>
    fun unhideSubject(id: UUID): OpinionSubjectDto
    fun unhideOpinionList(id: UUID): OpinionListSummaryDto
}
