package com.r8n.backend.users.integration

import com.r8n.backend.users.integration.api.UsersInternalApi
import com.r8n.backend.users.integration.api.UsersInternalApi.Companion.IS_ADMIN_PATH
import com.r8n.backend.users.integration.api.UsersInternalApi.Companion.IS_AI_MODERATOR_PATH
import com.r8n.backend.users.integration.api.UsersInternalApi.Companion.IS_ANY_MODERATOR_PATH
import com.r8n.backend.users.integration.api.UsersInternalApi.Companion.IS_HUMAN_MODERATOR_PATH
import com.r8n.backend.users.integration.api.UsersInternalApi.Companion.NAME_PATH
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.UUID

class UsersRestClient(
    private val restClient: RestClient,
) : UsersInternalApi {
    override fun getUserName(id: UUID): String =
        restClient
            .get()
            .uri(NAME_PATH, id)
            .retrieve()
            .body<String>()!!

    override fun isAnyModerator(id: UUID): Boolean =
        restClient
            .get()
            .uri(IS_ANY_MODERATOR_PATH, id)
            .retrieve()
            .body<Boolean>()!!

    override fun isHumanModerator(id: UUID): Boolean =
        restClient
            .get()
            .uri(IS_HUMAN_MODERATOR_PATH, id)
            .retrieve()
            .body<Boolean>()!!

    override fun isAiModerator(id: UUID): Boolean =
        restClient
            .get()
            .uri(IS_AI_MODERATOR_PATH, id)
            .retrieve()
            .body<Boolean>()!!

    override fun isAdmin(id: UUID): Boolean =
        restClient
            .get()
            .uri(IS_ADMIN_PATH, id)
            .retrieve()
            .body<Boolean>()!!
}