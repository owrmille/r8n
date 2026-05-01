package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.about.SelectorDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Tag(name = "Selectors", description = "Endpoints for retrieving and suggesting selectors for subjects and URLs.")
interface SelectorApi {
    companion object {
        private const val ROOT_PATH = "/api/selectors"
        const val FOR_URL_PATH = "$ROOT_PATH/for-url"
        const val FOR_SUBJECT_PATH = "$ROOT_PATH/for-subject/{subjectId}"
    }

    @GetMapping(FOR_URL_PATH)
    @Operation(
        summary = "Get selectors for URL",
        description = "Returns paged selectors associated with a specific URL.",
    )
    fun getForURL(
        @Parameter(description = "The URL to fetch selectors for.")
        @RequestParam(required = true)
        url: String,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<SelectorDto>

    @GetMapping(FOR_SUBJECT_PATH)
    @Operation(
        summary = "Get selectors for subject",
        description = "Returns paged selectors associated with a specific subject identifier.",
    )
    fun getForSubject(
        @Parameter(description = "Subject identifier.")
        @PathVariable subjectId: UUID,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<SelectorDto>

    @PostMapping(FOR_SUBJECT_PATH)
    @Operation(
        summary = "Suggest selector",
        description = "Submits a new selector suggestion for a specific subject.",
    )
    fun suggest(
        @Parameter(description = "Subject identifier.")
        @PathVariable
        subjectId: UUID,
        @Parameter(description = "The suggested selector string.")
        @RequestParam(required = true)
        selector: String,
    ): SelectorDto
}
