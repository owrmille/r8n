package com.r8n.backend.users.facade

import com.r8n.backend.users.api.dto.ConsentDto
import com.r8n.backend.users.api.dto.UpdateMyPublicProfileRequestDto
import com.r8n.backend.users.api.dto.UserDto
import com.r8n.backend.users.api.dto.UserProfileDto
import com.r8n.backend.users.api.dto.UserSessionDto
import com.r8n.backend.users.api.dto.UserStatusEnumDto
import com.r8n.backend.users.api.dto.UsernameDto
import com.r8n.backend.users.domain.Consent
import com.r8n.backend.users.domain.UserProfile
import com.r8n.backend.users.domain.UserSession
import com.r8n.backend.users.domain.UserStatusEnum
import com.r8n.backend.users.domain.Username
import com.r8n.backend.users.service.UserService
import com.r8n.backend.users.service.UserSessionService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserFacade(
    private val userService: UserService,
    private val sessionService: UserSessionService,
) {
    fun getMyName(userId: UUID): UsernameDto = userService.getMyName(userId).toDto()

    private fun Username.toDto() = UsernameDto(id, name)

    fun getUserProfile(id: UUID) = userService.getProfile(id).toDto()

    fun updateMyPublicProfile(
        userId: UUID,
        request: UpdateMyPublicProfileRequestDto,
    ) = userService
        .updateProfile(userId, request.name, request.about, request.location)
        .toDto()

    fun getSessionsForUser(
        id: UUID,
        pageable: Pageable,
    ): Page<UserSessionDto> = sessionService.getSessionsForUser(id, pageable).map { it.toDto() }

    fun getUser(id: UUID): UserDto =
        userService.getUser(id).let { user ->
            UserDto(
                id = user.id,
                name = user.name,
                email = user.email,
                status = user.status.toDto(),
                statusTimestamp = user.statusTimestamp,
                consents = user.consents.map { it.toDto() },
            )
        }

    private fun UserProfile.toDto() =
        UserProfileDto(
            id,
            name,
            status.toDto(),
            lastSeenAt,
            about,
            location,
        )

    private fun UserStatusEnum.toDto() =
        when (this) {
            UserStatusEnum.ACTIVE -> UserStatusEnumDto.ACTIVE
            UserStatusEnum.SUSPENDED -> UserStatusEnumDto.SUSPENDED
            UserStatusEnum.DELETION_PENDING -> UserStatusEnumDto.DELETION_PENDING
            UserStatusEnum.DELETED -> UserStatusEnumDto.DELETED
        }

    private fun Consent.toDto() =
        ConsentDto(
            type = type,
            accepted = accepted,
            session = session.toDto(),
        )

    private fun UserSession.toDto() =
        UserSessionDto(
            id = id,
            created = created,
            expires = expires,
            ip = ip,
            userAgent = userAgent,
        )
}