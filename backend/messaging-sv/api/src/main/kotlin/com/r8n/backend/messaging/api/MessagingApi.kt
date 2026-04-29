package com.r8n.backend.messaging.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.messaging.api.dto.messaging.CreateSupportMessageRequestDto
import com.r8n.backend.messaging.api.dto.messaging.CreateSupportThreadRequestDto
import com.r8n.backend.messaging.api.dto.messaging.SupportMessageDto
import com.r8n.backend.messaging.api.dto.messaging.SupportThreadSummaryDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

interface MessagingApi {
    companion object {
        private const val ROOT_PATH = "/api/messaging"
        const val SUPPORT_PATH = "$ROOT_PATH/support"
        const val SUPPORT_THREADS_PATH = "$SUPPORT_PATH/threads"
        const val SUPPORT_THREAD_PATH = "$SUPPORT_THREADS_PATH/{threadId}"
        const val SUPPORT_THREAD_MESSAGES_PATH = "$SUPPORT_THREADS_PATH/{threadId}/messages"
    }

    @GetMapping(SUPPORT_THREADS_PATH)
    fun getSupportThreadSummaries(
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<SupportThreadSummaryDto>

    @PostMapping(SUPPORT_THREADS_PATH)
    fun createSupportThread(
        @Valid
        @RequestBody
        request: CreateSupportThreadRequestDto,
    ): SupportThreadSummaryDto

    @GetMapping(SUPPORT_THREAD_MESSAGES_PATH)
    fun getSupportThreadMessages(
        @PathVariable
        threadId: UUID,
        @Valid
        pageable: PageRequestDto,
    ): PageResponseDto<SupportMessageDto>

    @DeleteMapping(SUPPORT_THREAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteSupportThread(
        @PathVariable
        threadId: UUID,
    )

    @PostMapping(SUPPORT_THREAD_MESSAGES_PATH)
    fun addSupportThreadMessage(
        @PathVariable
        threadId: UUID,
        @Valid
        @RequestBody
        request: CreateSupportMessageRequestDto,
    ): SupportMessageDto
}
