package com.r8n.backend.opinions.api.subjects

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionSubjectDto
import com.r8n.backend.opinions.api.subjects.dto.CreateSubjectRequestDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

interface SubjectsApi {
    companion object {
        private const val ROOT_PATH = "/api/subjects"
        const val FIND_PATH = "$ROOT_PATH/find"
        const val CREATE_PATH = ROOT_PATH
        const val SET_PRIMARY_REFERENT_PATH = "$ROOT_PATH/{subjectId}/set-primary-referent"
    }

    @GetMapping(FIND_PATH)
    fun findSubject(
        @RequestParam(required = false)
        query: String?,
        @RequestParam(required = false)
        referentId: UUID?,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionSubjectDto>

    @PostMapping(CREATE_PATH)
    fun createSubject(
        @Valid
        @RequestBody
        request: CreateSubjectRequestDto,
    ): OpinionSubjectDto

    @PatchMapping(SET_PRIMARY_REFERENT_PATH)
    fun setPrimaryReferent(
        @PathVariable subjectId: UUID,
        @RequestParam(required = true) referentId: UUID,
    ): OpinionSubjectDto
}
