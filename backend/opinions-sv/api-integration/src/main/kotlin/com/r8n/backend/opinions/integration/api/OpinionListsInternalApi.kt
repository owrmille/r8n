package com.r8n.backend.opinions.integration.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface OpinionListsInternalApi {
    companion object {
        const val MINE_FULL_PATH = "/api/internal/opinion-lists/mine/full"
        const val USER_PATH = "/api/internal/opinion-lists/user/{userId}"
    }

    @GetMapping(MINE_FULL_PATH)
    fun getMineFull(pageable: PageRequestDto): PageResponseDto<OpinionListDto>

    @DeleteMapping(USER_PATH)
    fun deleteAllUserDataForUser(
        @PathVariable userId: UUID,
    )
}
}
