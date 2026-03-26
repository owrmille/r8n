package com.r8n.backend.users.integration

import com.r8n.backend.users.integration.UsersInternalApi.Companion.NAME_PATH
import org.springframework.web.client.RestClient
import java.util.UUID

class UsersRestClient(
    private val restClient: RestClient,
) : UsersInternalApi {
    override fun getUserName(id: UUID): String =
        restClient
            .get()
            .uri(NAME_PATH, id)
            .retrieve()
            .body(String::class.java)!!
}