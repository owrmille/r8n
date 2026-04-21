package com.r8n.backend.users.integration.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.users.api.dto.UserDto
import com.r8n.backend.users.api.dto.UserSessionDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface UsersInternalApi {
    companion object {
        private const val ROOT_PATH = "/api/internal/sers"
        const val NAME_PATH = "$ROOT_PATH/{id}/name"
        const val IS_ANY_MODERATOR_PATH = "$ROOT_PATH/{id}/is-any-moderator"
        const val IS_HUMAN_MODERATOR_PATH = "$ROOT_PATH/{id}/is-human-moderator"
        const val IS_AI_MODERATOR_PATH = "$ROOT_PATH/{id}/is-ai-moderator"
        const val IS_ADMIN_PATH = "$ROOT_PATH/{id}/is-admin"
    }

    @GetMapping(NAME_PATH)
    fun getUserName(
        @PathVariable id: UUID,
    ): String

    @GetMapping(ID_PATH)
    fun getUser(
        @PathVariable id: UUID,
    ): UserDto

    @GetMapping(SESSIONS_PATH)
    fun getSessionsForUser(
        @PathVariable id: UUID,
        page: PageRequestDto? = null,
    ): PageResponseDto<UserSessionDto>

    @GetMapping(IS_ANY_MODERATOR_PATH)
    fun isAnyModerator(
        @PathVariable id: UUID,
    ): Boolean

    @GetMapping(IS_HUMAN_MODERATOR_PATH)
    fun isHumanModerator(
        @PathVariable id: UUID,
    ): Boolean

    @GetMapping(IS_AI_MODERATOR_PATH)
    fun isAiModerator(
        @PathVariable id: UUID,
    ): Boolean

    @GetMapping(IS_ADMIN_PATH)
    fun isAdmin(
        @PathVariable id: UUID,
    ): Boolean
}