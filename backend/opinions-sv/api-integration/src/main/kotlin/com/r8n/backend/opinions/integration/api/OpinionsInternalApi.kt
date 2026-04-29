package com.r8n.backend.opinions.integration.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface OpinionsInternalApi {
    companion object {
        const val MINE_FULL_PATH = "/api/internal/opinions/mine"
        const val RESTORE_PATH = "/api/internal/opinions/restore"
    }

    @GetMapping(MINE_FULL_PATH)
    fun getMyFullOpinions(
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionDto>
}
