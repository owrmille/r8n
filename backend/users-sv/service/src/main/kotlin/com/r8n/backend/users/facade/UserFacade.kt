package com.r8n.backend.users.facade

import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.opinions.api.access.IncomingAccessRequestApi
import com.r8n.backend.opinions.api.access.OutgoingAccessRequestApi
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.users.api.dto.UserProfileDto
import com.r8n.backend.users.api.dto.UserStatusEnumDto
import com.r8n.backend.users.api.dto.UsernameDto
import com.r8n.backend.users.domain.UserProfile
import com.r8n.backend.users.domain.UserStatusEnum
import com.r8n.backend.users.domain.Username
import com.r8n.backend.users.service.UserService
import com.r8n.backend.users.service.UserSessionService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserFacade(
    private val userService: UserService,
    private val sessionService: UserSessionService,
    private val opinionClient: OpinionListsInternalApi,
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

    private fun UserStatusEnum.toDto() = UserStatusEnumDto.valueOf(this.name)
}