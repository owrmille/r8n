package com.r8n.backend.users.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class AccountDeletionRequestDto(
    @field:NotBlank
    @field:Email
    val email: String,
)
