package com.r8n.backend.users.integration

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface UsersInternalApi {
    companion object {
        const val NAME_PATH = "/users/name/{id}"
    }

    @GetMapping(NAME_PATH)
    fun getUserName(
        @PathVariable id: UUID,
    ): String
}