package com.r8n.backend.opinions.facade

import com.r8n.backend.users.integration.UsersInternalApi
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID

@Profile("local")
@Component
class UsersInternalApiStub : UsersInternalApi {
    override fun getUserName(id: UUID) = "Lorem Ipsum"
}