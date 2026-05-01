package com.r8n.backend.opinions.api.referents

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.opinions.dto.ReferentDto
import com.r8n.backend.opinions.api.referents.dto.CreateReferentRequestDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Referents", description = "Referents used for categorizing and identifying opinion subjects.")
interface ReferentsApi {
    companion object {
        private const val ROOT_PATH = "/api/referents"
        const val FIND_PATH = "$ROOT_PATH/find"
        const val CREATE_PATH = ROOT_PATH
    }

    @GetMapping(FIND_PATH)
    @Operation(
        summary = "Find referents",
        description = "Searches for referents by query string with paged results.",
    )
    fun findReferents(
        @Parameter(description = "Search query string.")
        @RequestParam(required = false)
        query: String?,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<ReferentDto>

    @PostMapping(CREATE_PATH)
    @Operation(
        summary = "Create referent",
        description = "Creates a new referent for categorizing subjects.",
    )
    fun createReferent(
        @Valid
        @RequestBody
        request: CreateReferentRequestDto,
    ): ReferentDto
}
