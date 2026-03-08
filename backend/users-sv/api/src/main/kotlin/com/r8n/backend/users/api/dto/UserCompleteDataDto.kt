package com.r8n.backend.users.api.dto

import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.SupportThreadDto
import com.r8n.backend.mock.api.dto.access.AccessRequestDto
import com.r8n.backend.mock.api.dto.list.OpinionListDto
import java.time.Instant
import java.util.UUID

data class UserCompleteDataDto(
    val id: UUID,
    val status: UserStatusEnum,
    val statusTimestamp: Instant,
    val consents: ConsentDto,
    val personalIdentifiableInformation: PersonalIdentifiableInformationSectionDto,
    val opinions: PageResponseDto<OpinionListDto>,
    val outgoingRequests: PageResponseDto<AccessRequestDto>,
    val incomingRequests: PageResponseDto<AccessRequestDto>,
    val messages: PageResponseDto<SupportThreadDto>,
)

enum class UserStatusEnum {
    ACTIVE,
    SUSPENDED,
    DELETED,
}
