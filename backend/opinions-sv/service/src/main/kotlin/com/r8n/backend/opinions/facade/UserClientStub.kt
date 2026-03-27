package com.r8n.backend.opinions.facade

import com.r8n.backend.mock.integration.UserClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID

@Profile("mocked-users")
@Component
class UserClientStub: UserClient {
    override fun getUserName(id: UUID) = "Lorem Ipsum"
}