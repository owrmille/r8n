package com.r8n.backend.messaging.integration.client.internal

import com.r8n.backend.messaging.integration.api.MessagingInternalApi
import com.r8n.backend.messaging.integration.api.MessagingInternalApi.Companion.USER_PATH
import org.springframework.web.client.RestClient
import java.util.UUID

class MessagingInternalRestClient(
    private val restClient: RestClient,
) : MessagingInternalApi {
    override fun deleteAllUserDataForUser(userId: UUID) {
        restClient
            .delete()
            .uri(USER_PATH, userId)
            .retrieve()
            .toBodilessEntity()
    }
}
