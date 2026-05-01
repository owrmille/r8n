package com.r8n.backend.opinions.integration.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Tag(
    name = "Internal opinion lists deletion",
    description = "Internal opinion-list deletion endpoints for service-to-service communication.",
)
interface OpinionListsDeletionInternalApi {
    companion object {
        const val USER_PATH = "/api/internal/opinion-lists/user/{userId}"
    }

    @DeleteMapping(USER_PATH)
    @Operation(
        summary = "Delete all user opinion-list data (internal)",
        description =
            "Deletes opinion lists, linked opinions, and access requests for a user. " +
                "This is an internal endpoint for service-to-service account deletion.",
    )
    fun deleteAllUserDataForUser(
        @Parameter(description = "User identifier.")
        @PathVariable userId: UUID,
    )
}
