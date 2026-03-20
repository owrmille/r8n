package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.SupportThreadDto
import com.r8n.backend.mock.api.dto.about.SelectorDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

interface SelectorApi {
    companion object {
        const val FOR_URL_PATH = "/selectors/for-url"
        const val FOR_SUBJECT_PATH = "/selectors/for-subject/{subjectId}"
        const val DISAGREE_PATH = "/selectors/{selectorId}/disagree"
    }

    @GetMapping(FOR_URL_PATH)
    fun getForURL(
        @RequestParam(required = true)
        url: String,
        pageable: PageRequestDto,
    ): PageResponseDto<SelectorDto>

    @GetMapping(FOR_SUBJECT_PATH)
    fun getForSubject(
        @PathVariable subjectId: UUID,
        pageable: PageRequestDto,
    ): PageResponseDto<SelectorDto>

    @PostMapping(FOR_SUBJECT_PATH)
    fun suggest(
        @PathVariable
        subjectId: UUID,
        @RequestParam(required = true)
        selector: String,
    ): SelectorDto

    @PostMapping(DISAGREE_PATH)
    fun disagree(
        @PathVariable
        selectorId: UUID,
        @RequestParam(required = true)
        comment: String?,
    ): SupportThreadDto
}