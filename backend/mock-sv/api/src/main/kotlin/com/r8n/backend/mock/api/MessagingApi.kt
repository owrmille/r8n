package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.SupportThreadDto
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface MessagingApi {
    companion object {
        private const val ROOT_PATH = "/api/messaging"
        const val SUPPORT_PATH = "$ROOT_PATH/support"
        const val USER_PATH = "$ROOT_PATH/user/{userId}"
    }

    @GetMapping(SUPPORT_PATH)
    fun getSupportThreads(): PageResponseDto<SupportThreadDto>

    @DeleteMapping(USER_PATH)
    fun deleteAllUserDataForUser(
        @PathVariable userId: UUID,
    )
}