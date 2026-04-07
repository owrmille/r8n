package com.r8n.backend.mock.integration.client

import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.MessagingApi.Companion.SUPPORT_PATH
import com.r8n.backend.mock.api.dto.SupportThreadDto
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class MessagingRestClient(
    private val restClient: RestClient,
) : MessagingApi {
    override fun getSupportThreads(): PageResponseDto<SupportThreadDto> =
        restClient
            .get()
            .uri(SUPPORT_PATH)
            .retrieve()
            .body<PageResponseDto<SupportThreadDto>>()!!
}