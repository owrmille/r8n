package com.r8n.backend.users.facade

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.api.IncomingAccessRequestApi
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.OutgoingAccessRequestApi
import com.r8n.backend.mock.integration.api.OpinionListInternalApi
import com.r8n.backend.users.api.dto.ConsentDto
import com.r8n.backend.users.api.dto.PersonalIdentifiableInformationSectionDto
import com.r8n.backend.users.api.dto.UserCompleteDataDto
import com.r8n.backend.users.api.dto.UserSessionDto
import com.r8n.backend.users.api.dto.UserStatusEnumDto
import com.r8n.backend.users.domain.Consent
import com.r8n.backend.users.domain.UserSession
import com.r8n.backend.users.domain.UserStatusEnum
import com.r8n.backend.users.service.UserService
import com.r8n.backend.users.service.UserSessionService
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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
    fun getUserCompleteDataDto(id: UUID): UserCompleteDataDto {
        val user = userService.getUser(id)
        val sessions = sessionService.getSessionsForUser(id, Pageable.unpaged())

        return UserCompleteDataDto(
            id = user.id,
            status = user.status.toDto(),
            statusTimestamp = user.statusTimestamp,
            consents = PageImpl(user.consents.map { it.toDto() }).toResponse(),
            personalIdentifiableInformation = PersonalIdentifiableInformationSectionDto(
                name = user.name,
                email = user.email,
                sessions = sessions.map { it.toDto() }.toResponse(),
            ),
            opinions = opinionClient.getMineFull(PageRequestDto(0, -1)),
            outgoingRequests = outgoingAccessRequestClient.get(null, null, null, PageRequestDto(0, -1)),
            incomingRequests = incomingAccessRequestClient.get(null, null, null, PageRequestDto(0, -1)),
            messages = messageClient.getSupportThreads(),
        )
    }

    private fun UserStatusEnum.toDto() = UserStatusEnumDto.valueOf(this.name)
    private fun Consent.toDto() = ConsentDto(type, accepted, session.toDto())
    private fun UserSession.toDto() = UserSessionDto(id, created, expires, ip, userAgent)
}