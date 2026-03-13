package com.r8n.backend.users.domain

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val name: UUID,
    val status: UserStatusEnum,
    val statusTimestamp: Instant,
    val consents
)

enum class UserStatusEnum {
    ACTIVE,
    SUSPENDED,
    DELETION_PENDING,
    DELETED,
}
