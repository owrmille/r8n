package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.SupportThreadDto
import org.springframework.web.bind.annotation.GetMapping

interface MessagingApi {
    companion object {
        const val MESSAGING_PATH = "/messaging"
        const val SUPPORT_PATH = "$MESSAGING_PATH/support"
    }

    @GetMapping(SUPPORT_PATH)
    fun getSupportThreads(): PageResponseDto<SupportThreadDto>
}