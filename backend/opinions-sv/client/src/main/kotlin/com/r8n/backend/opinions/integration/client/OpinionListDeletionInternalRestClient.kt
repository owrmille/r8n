package com.r8n.backend.opinions.integration.client

import com.r8n.backend.opinions.integration.api.OpinionListsDeletionInternalApi
import com.r8n.backend.opinions.integration.api.OpinionListsDeletionInternalApi.Companion.USER_PATH
import org.springframework.web.client.RestClient
import java.util.UUID

class OpinionListDeletionInternalRestClient(
    private val restClient: RestClient,
) : OpinionListsDeletionInternalApi {
    override fun deleteAllUserDataForUser(userId: UUID) {
        restClient
            .delete()
            .uri(USER_PATH, userId)
            .retrieve()
            .toBodilessEntity()
    }
}
