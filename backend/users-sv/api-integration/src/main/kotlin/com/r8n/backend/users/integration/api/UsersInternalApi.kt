package com.r8n.backend.users.integration.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface UsersInternalApi {
    companion object {
        private const val ROOT_PATH = "/api/users"
        const val NAME_PATH = "$ROOT_PATH/{id}/name"
    }

    @GetMapping(NAME_PATH)
    fun getUserName(
        @PathVariable id: UUID,
    ): String
}