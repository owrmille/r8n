package com.r8n.backend.users.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateMyPublicProfileRequestDto(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,
    @field:Size(max = 2000)
    val about: String?,
    @field:Size(max = 255)
    val location: String?,
)
