package com.r8n.backend.opinions.integration.client

import com.r8n.backend.opinions.integration.api.OpinionsInternalApi
import com.r8n.backend.opinions.integration.api.OpinionsInternalApi.Companion.OPINION_OWNER_PATH
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.UUID

class OpinionsInternalRestClient(
    private val restClient: RestClient,
) : OpinionsInternalApi {
    override fun getOwnerOfOpinion(id: UUID): UUID =
        restClient
            .get()
            .uri(OPINION_OWNER_PATH, id)
            .retrieve()
            .body<UUID>()!!
}