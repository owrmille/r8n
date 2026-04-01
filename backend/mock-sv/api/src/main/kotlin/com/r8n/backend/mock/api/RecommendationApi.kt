package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.list.OpinionListSummaryDto
import com.r8n.backend.opinions.api.dto.about.OpinionSubjectDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface RecommendationApi {
    companion object {
        const val SUBJECTS_PATH = "/recommendations/subjects/{lookingAtSubjectId}"
        const val OPINION_LISTS_PATH = "/recommendations/opinion-lists/{lookingAtListId}"
        const val HIDE_SUBJECT_PATH = "/recommendations/subjects/{id}/hide"
        const val HIDE_LIST_PATH = "/recommendations/opinion-lists/{id}/hide"
        const val HIDDEN_SUBJECTS_PATH = "/recommendations/subjects/hidden"
        const val HIDDEN_LISTS_PATH = "/recommendations/opinion-lists/hidden"
        const val UNHIDE_SUBJECT_PATH = "/recommendations/subjects/{id}/unhide"
        const val UNHIDE_LIST_PATH = "/recommendations/opinion-lists/{id}/unhide"
    }

    @GetMapping(SUBJECTS_PATH)
    fun getRecommendedSubjects(
        @PathVariable lookingAtSubjectId: UUID,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionSubjectDto>

    @GetMapping(OPINION_LISTS_PATH)
    fun getRecommendedOpinionLists(
        @PathVariable lookingAtListId: UUID,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionListSummaryDto>

    @PatchMapping(HIDE_SUBJECT_PATH)
    fun hideRecommendedSubject(@PathVariable id: UUID)

    @PatchMapping(HIDE_LIST_PATH)
    fun hideRecommendedOpinionList(@PathVariable id: UUID)

    @GetMapping(HIDDEN_SUBJECTS_PATH)
    fun getHiddenSubjects(pageable: PageRequestDto): PageResponseDto<OpinionSubjectDto>

    @GetMapping(HIDDEN_LISTS_PATH)
    fun getHiddenOpinionLists(pageable: PageRequestDto): PageResponseDto<OpinionListSummaryDto>

    @PatchMapping(UNHIDE_SUBJECT_PATH)
    fun unhideSubject(@PathVariable id: UUID): OpinionSubjectDto

    @PatchMapping(UNHIDE_LIST_PATH)
    fun unhideOpinionList(@PathVariable id: UUID): OpinionListSummaryDto
}
