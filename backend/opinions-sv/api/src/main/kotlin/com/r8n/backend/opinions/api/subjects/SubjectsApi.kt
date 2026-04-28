package com.r8n.backend.opinions.api.subjects

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionSubjectDto
import com.r8n.backend.opinions.api.subjects.dto.CreateSubjectRequestDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

interface SubjectsApi {
    companion object {
        private const val ROOT_PATH = "/api/subjects"
        const val FIND_PATH = "$ROOT_PATH/find"
        const val CREATE_PATH = ROOT_PATH
    }

    @GetMapping(FIND_PATH)
    fun findSubject(
        @RequestParam(required = true)
        @NotBlank
        query: String,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionSubjectDto>

    @PostMapping(CREATE_PATH)
    fun createSubject(
        @Valid
        @RequestBody
        request: CreateSubjectRequestDto,
    ): OpinionSubjectDto
}
