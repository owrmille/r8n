package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.SupportThreadDto
import org.springframework.web.bind.annotation.GetMapping

interface MessagingApi {
    companion object {
        private const val ROOT_PATH = "/api/messaging"
        const val SUPPORT_PATH = "$ROOT_PATH/support"
    }

    @GetMapping(SUPPORT_PATH)
    fun getSupportThreads(): PageResponseDto<SupportThreadDto>
}