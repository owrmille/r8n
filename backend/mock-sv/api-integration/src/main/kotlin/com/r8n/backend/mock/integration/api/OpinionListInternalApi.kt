package com.r8n.backend.mock.integration.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.list.OpinionListDto
import org.springframework.web.bind.annotation.GetMapping

interface OpinionListInternalApi {
    companion object {
        const val MINE_FULL_PATH = "/api/opinion-lists/mine/full"
    }

    @GetMapping(MINE_FULL_PATH)
    fun getMineFull(pageable: PageRequestDto): PageResponseDto<OpinionListDto>
}