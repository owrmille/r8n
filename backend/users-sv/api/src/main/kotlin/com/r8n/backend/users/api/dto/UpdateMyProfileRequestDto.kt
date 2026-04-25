package com.r8n.backend.users.api.dto

data class UpdateMyProfileRequestDto(
    val name: String,
    val about: String?,
    val location: String?,
)
