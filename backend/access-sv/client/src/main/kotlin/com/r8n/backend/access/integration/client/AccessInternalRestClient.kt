package com.r8n.backend.access.integration.client

import com.r8n.backend.access.integration.api.AccessInternalApi
import com.r8n.backend.access.integration.api.AccessInternalApi.Companion.ACCESS_OPINION_LIST_PATH
import com.r8n.backend.access.integration.api.AccessInternalApi.Companion.ACCESS_OPINION_PATH
import com.r8n.backend.access.integration.api.dto.OpinionPermissionEnumDto
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.UUID

class AccessInternalRestClient(
    private val restClient: RestClient,
) : AccessInternalApi {
    override fun canAccessOpinion(
        permission: OpinionPermissionEnumDto,
        opinionId: UUID,
    ): Boolean =
        restClient
            .get()
            .uri(ACCESS_OPINION_PATH, opinionId)
            .header("X-Permission", permission.name)
            .retrieve()
            .body<Boolean>()!!

    override fun canAccessOpinionList(
        permission: OpinionPermissionEnumDto,
        opinionListId: UUID,
    ): Boolean =
        restClient
            .get()
            .uri(ACCESS_OPINION_LIST_PATH, opinionListId)
            .header("X-Permission", permission.name)
            .retrieve()
            .body<Boolean>()!!
}