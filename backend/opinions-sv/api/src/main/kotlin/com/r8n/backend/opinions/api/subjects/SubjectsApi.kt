package com.r8n.backend.opinions.api.subjects

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionSubjectDto
import com.r8n.backend.opinions.api.subjects.dto.CreateSubjectRequestDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Subjects", description = "Reviewable subjects that opinions can be written about.")
interface SubjectsApi {
    companion object {
        private const val ROOT_PATH = "/api/subjects"
        const val FIND_PATH = "$ROOT_PATH/find"
        const val CREATE_PATH = ROOT_PATH
    }

    @GetMapping(FIND_PATH)
    @Operation(
        summary = "Find subjects",
        description = "Searches reviewable subjects by text and returns paged matches.",
    )
    fun findSubject(
        @Parameter(description = "Text to search for in subject data.")
        @RequestParam(required = true)
        @NotBlank
        query: String,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionSubjectDto>

    @PostMapping(CREATE_PATH)
    @Operation(
        summary = "Create subject",
        description = "Creates a reviewable subject when it does not already exist.",
    )
    fun createSubject(
        @Valid
        @RequestBody
        request: CreateSubjectRequestDto,
    ): OpinionSubjectDto
}
