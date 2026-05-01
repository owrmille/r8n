package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListSummaryDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionSubjectDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Tag(name = "Recommendations", description = "Endpoints for discovering recommended subjects and opinion lists.")
interface RecommendationApi {
    companion object {
        private const val ROOT_PATH = "/api/recommendations"
        const val SUBJECTS_PATH = "$ROOT_PATH/subjects/{lookingAtSubjectId}"
        const val OPINION_LISTS_PATH = "$ROOT_PATH/opinion-lists/{lookingAtListId}"
        const val HIDE_SUBJECT_PATH = "$ROOT_PATH/subjects/{id}/hide"
        const val HIDE_LIST_PATH = "$ROOT_PATH/opinion-lists/{id}/hide"
        const val HIDDEN_SUBJECTS_PATH = "$ROOT_PATH/subjects/hidden"
        const val HIDDEN_LISTS_PATH = "$ROOT_PATH/opinion-lists/hidden"
        const val UNHIDE_SUBJECT_PATH = "$ROOT_PATH/subjects/{id}/unhide"
        const val UNHIDE_LIST_PATH = "$ROOT_PATH/opinion-lists/{id}/unhide"
    }

    @GetMapping(SUBJECTS_PATH)
    @Operation(
        summary = "Get recommended subjects",
        description = "Returns paged subject recommendations based on the subject currently being viewed.",
    )
    fun getRecommendedSubjects(
        @Parameter(description = "The identifier of the subject currently being viewed.")
        @PathVariable lookingAtSubjectId: UUID,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionSubjectDto>

    @GetMapping(OPINION_LISTS_PATH)
    @Operation(
        summary = "Get recommended opinion lists",
        description = "Returns paged opinion list recommendations based on the list currently being viewed.",
    )
    fun getRecommendedOpinionLists(
        @Parameter(description = "The identifier of the opinion list currently being viewed.")
        @PathVariable lookingAtListId: UUID,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionListSummaryDto>

    @PatchMapping(HIDE_SUBJECT_PATH)
    @Operation(
        summary = "Hide recommended subject",
        description = "Hides a subject from future recommendations for the authenticated user.",
    )
    fun hideRecommendedSubject(
        @Parameter(description = "Subject identifier to hide.")
        @PathVariable id: UUID,
    )

    @PatchMapping(HIDE_LIST_PATH)
    @Operation(
        summary = "Hide recommended opinion list",
        description = "Hides an opinion list from future recommendations for the authenticated user.",
    )
    fun hideRecommendedOpinionList(
        @Parameter(description = "Opinion list identifier to hide.")
        @PathVariable id: UUID,
    )

    @GetMapping(HIDDEN_SUBJECTS_PATH)
    @Operation(
        summary = "List hidden subjects",
        description = "Returns paged subjects that have been hidden from recommendations by the authenticated user.",
    )
    fun getHiddenSubjects(
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionSubjectDto>

    @GetMapping(HIDDEN_LISTS_PATH)
    @Operation(
        summary = "List hidden opinion lists",
        description =
            "Returns paged opinion lists that have been hidden from recommendations by the authenticated user.",
    )
    fun getHiddenOpinionLists(
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionListSummaryDto>

    @PatchMapping(UNHIDE_SUBJECT_PATH)
    @Operation(
        summary = "Unhide subject",
        description = "Restores a hidden subject to future recommendations.",
    )
    fun unhideSubject(
        @Parameter(description = "Subject identifier to unhide.")
        @PathVariable id: UUID,
    ): OpinionSubjectDto

    @PatchMapping(UNHIDE_LIST_PATH)
    @Operation(
        summary = "Unhide opinion list",
        description = "Restores a hidden opinion list to future recommendations.",
    )
    fun unhideOpinionList(
        @Parameter(description = "Opinion list identifier to unhide.")
        @PathVariable id: UUID,
    ): OpinionListSummaryDto
}
