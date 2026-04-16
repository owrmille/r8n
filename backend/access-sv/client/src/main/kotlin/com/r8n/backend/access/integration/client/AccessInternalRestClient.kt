package com.r8n.backend.access.integration.client

import com.r8n.backend.access.integration.api.AccessInternalApi
import org.springframework.web.client.RestClient

class AccessInternalRestClient(
    private val restClient: RestClient,
) : AccessInternalApi {

}