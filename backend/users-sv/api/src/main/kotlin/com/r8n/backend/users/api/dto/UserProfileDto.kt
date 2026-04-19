package com.r8n.backend.users.api.dto

import java.time.Instant
import java.util.UUID

data class UserProfileDto(
    val id: UUID,
    val name: String,
    val status: UserStatusEnumDto,
    val lastOnline: Instant?,
    val about: String?,
    val location: String?,
)