package com.r8n.backend.users.api.dto

import java.util.UUID

data class UsernameAndEmailDto(
    val id: UUID,
    val name: String,
    val email: String,
    val roles: List<RoleEnumDto>,
)
