package com.r8n.backend.opinions.integration.api

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface OpinionListsDeletionInternalApi {
    companion object {
        const val USER_PATH = "/api/internal/opinion-lists/user/{userId}"
    }

    @DeleteMapping(USER_PATH)
    fun deleteAllUserDataForUser(
        @PathVariable userId: UUID,
    )
}
