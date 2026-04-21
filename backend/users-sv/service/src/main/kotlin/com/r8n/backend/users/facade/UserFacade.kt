package com.r8n.backend.users.facade

import com.r8n.backend.mock.api.IncomingAccessRequestApi
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.OutgoingAccessRequestApi
import com.r8n.backend.mock.integration.api.OpinionListInternalApi
import com.r8n.backend.users.api.dto.UserProfileDto
import com.r8n.backend.users.api.dto.UsernameDto
import com.r8n.backend.users.domain.UserProfile
import com.r8n.backend.users.domain.Username
import com.r8n.backend.users.service.UserService
import com.r8n.backend.users.service.UserSessionService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserFacade(
    private val userService: UserService,
    private val sessionService: UserSessionService,
    private val opinionClient: OpinionListInternalApi,
    private val incomingAccessRequestClient: IncomingAccessRequestApi,
    private val outgoingAccessRequestClient: OutgoingAccessRequestApi,
    private val messageClient: MessagingApi,
) {
    fun getMyName(): UsernameDto = userService.getMyName().toDto()

    private fun Username.toDto() = UsernameDto(id, name)

    fun getUserProfile(id: UUID) = userService.getProfile(id).toDto()

    private fun UserProfile.toDto() =
        UserProfileDto(
            id,
            name,
            status.toDto(),
            lastOnline,
            about,
            location,
        )
}