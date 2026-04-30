package com.r8n.backend.messaging.integration.client.internal

import com.r8n.backend.messaging.api.dto.messaging.CreateSupportMessageRequestDto
import com.r8n.backend.messaging.api.dto.messaging.SupportMessageDto
import com.r8n.backend.messaging.integration.api.MessagingInternalApi
import com.r8n.backend.messaging.integration.api.MessagingInternalApi.Companion.SUPPORT_THREAD_MESSAGES_PATH
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.UUID

class MessagingInternalRestClient(
    private val restClient: RestClient,
) : MessagingInternalApi {
    override fun addSupportThreadMessage(
        threadId: UUID,
        request: CreateSupportMessageRequestDto,
    ): SupportMessageDto =
        restClient
            .post()
            .uri(SUPPORT_THREAD_MESSAGES_PATH, threadId)
            .body(request)
            .retrieve()
            .body<SupportMessageDto>()!!
}
