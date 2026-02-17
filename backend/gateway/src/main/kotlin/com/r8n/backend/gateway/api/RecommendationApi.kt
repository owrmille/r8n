package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.PageResponse
import com.r8n.backend.gateway.api.dto.list.OpinionListSummaryDto
import com.r8n.backend.gateway.api.dto.about.OpinionSubjectDto
import org.springframework.data.domain.Pageable
import java.util.UUID

interface RecommendationApi {
    fun getRecommendedSubjects(lookingAtSubjectId: UUID, pageable: Pageable): PageResponse<OpinionSubjectDto>
    fun getRecommendedOpinionLists(lookingAtListId: UUID, pageable: Pageable): PageResponse<OpinionListSummaryDto>
    fun hideRecommendedSubject(id: UUID)
    fun hideRecommendedOpinionList(id: UUID)
    fun getHiddenSubjects(pageable: Pageable): PageResponse<OpinionSubjectDto>
    fun getHiddenOpinionLists(pageable: Pageable): PageResponse<OpinionListSummaryDto>
    fun unhideSubject(id: UUID): OpinionSubjectDto
    fun unhideOpinionList(id: UUID): OpinionListSummaryDto
}
