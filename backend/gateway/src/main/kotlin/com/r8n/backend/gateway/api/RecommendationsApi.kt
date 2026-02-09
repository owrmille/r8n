package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.list.OpinionListSummaryDto
import com.r8n.backend.gateway.api.dto.about.OpinionSubjectDto
import java.util.UUID

interface RecommendationsApi {
    fun getRecommendedSubjects(lookingAtSubjectId: UUID): List<OpinionSubjectDto>
    fun getRecommendedOpinionLists(lookingAtListId: UUID): List<OpinionListSummaryDto>
    fun hideRecommendedSubject(id: UUID)
    fun hideRecommendedOpinionList(id: UUID)
    fun getHiddenItems(): List<OpinionSubjectDto>
    fun getHiddenOpinionLists(): List<OpinionListSummaryDto>
    fun unhideItem(id: UUID)
    fun unhideOpinionList(id: UUID)
}
