package com.r8n.backend.messaging.integration.api

import com.r8n.backend.messaging.api.dto.messaging.CreateSupportMessageRequestDto
import com.r8n.backend.messaging.api.dto.messaging.SupportMessageDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Tag(name = "Internal messaging", description = "Internal messaging endpoints for service-to-service communication.")
interface MessagingInternalApi {
    companion object {
        private const val ROOT_PATH = "/api/internal/messaging"
        const val SUPPORT_THREAD_MESSAGES_PATH = "$ROOT_PATH/support/threads/{threadId}/messages"
        const val USER_PATH = "$ROOT_PATH/user/{userId}"
    }

    @PostMapping(SUPPORT_THREAD_MESSAGES_PATH)
    @Operation(
        summary = "Add a support message (internal)",
        description =
            "Adds a message to an existing support thread. " +
                "This is an internal endpoint for service-to-service communication.",
    )
    fun addSupportThreadMessage(
        @Parameter(description = "Support thread identifier.")
        @PathVariable
        threadId: UUID,
        @Valid
        @RequestBody
        request: CreateSupportMessageRequestDto,
    ): SupportMessageDto

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
