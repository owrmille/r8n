package com.r8n.backend.users.facade

import com.r8n.backend.users.api.dto.UserCompleteDataDto
import com.r8n.backend.users.service.UsersService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UsersFacade(
    private val usersService: UsersService,
    private val opinionsClient: OpinionsClient,
    private val accessRequestsClient: AccessRequestsClient,
    private val messageClient: MessageClient,
) {
    fun getUserCompleteDataDto(id: UUID): UserCompleteDataDto {
        val usr = usersService.getUser(id)
        val res = UserCompleteDataDto(
            usr.id,
            usr.status,
            usr.statusTimestamp,
            usr.consents.toDto(),
            usr.pii.toDto(),
            opinionsClient.
        )
    }
}