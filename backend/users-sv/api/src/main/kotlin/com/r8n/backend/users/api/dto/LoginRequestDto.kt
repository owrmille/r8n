package com.r8n.backend.users.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequestDto(
    @field:NotBlank
    @field:Size(max = 254)
    val login: String,
    @field:NotBlank
    @field:Size(max = 128)
    val password: String,
)
