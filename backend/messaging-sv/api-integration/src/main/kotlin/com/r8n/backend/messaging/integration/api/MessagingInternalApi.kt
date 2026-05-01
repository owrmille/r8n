package com.r8n.backend.messaging.integration.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Tag(name = "Internal messaging", description = "Internal messaging endpoints for service-to-service communication.")
interface MessagingInternalApi {
    companion object {
        private const val ROOT_PATH = "/api/internal/messaging"
        const val USER_PATH = "$ROOT_PATH/user/{userId}"
    }

    @DeleteMapping(USER_PATH)
    @Operation(
        summary = "Delete all user data (internal)",
        description =
            "Deletes all messaging data for a user. " +
                "This is an internal endpoint for service-to-service communication.",
    )
    fun deleteAllUserDataForUser(
        @Parameter(description = "User identifier.")
        @PathVariable
        userId: UUID,
    )
}
