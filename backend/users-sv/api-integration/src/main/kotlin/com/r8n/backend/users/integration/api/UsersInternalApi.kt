package com.r8n.backend.users.integration.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface UsersInternalApi {
    companion object {
        private const val ROOT_PATH = "/api/users"
        const val NAME_PATH = "$ROOT_PATH/{id}/name"
        const val IS_MODERATOR_PATH = "$ROOT_PATH/{id}/is-moderator"
        const val IS_AI_MODERATOR_PATH = "$ROOT_PATH/{id}/is-ai-moderator"
        const val IS_ADMIN_PATH = "$ROOT_PATH/{id}/is-admin"
    }

    @GetMapping(NAME_PATH)
    fun getUserName(
        @PathVariable id: UUID,
    ): String

    @GetMapping(IS_MODERATOR_PATH)
    fun isModerator(
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