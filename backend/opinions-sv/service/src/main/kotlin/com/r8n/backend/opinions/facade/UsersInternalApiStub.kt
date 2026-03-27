package com.r8n.backend.opinions.facade

import com.r8n.backend.mock.integration.UserClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID

@Profile("mocked-users")
@Component
class UserClientStub: UserClient {
    private val namesById = mapOf(
        UUID.fromString("07070707-0707-0707-0707-070707070707") to "Alexander",
        UUID.fromString("10101010-1010-1010-1010-101010101010") to "Bernard",
        UUID.fromString("22222222-2222-2222-2222-222222222222") to "Donald John Trump",
    )

    override fun getUserName(id: UUID) = namesById[id] ?: "Lorem Ipsum"
}
