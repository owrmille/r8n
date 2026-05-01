package com.r8n.backend.users.api.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequestDto(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,
    @field:NotBlank
    @field:Email
    @field:Size(max = 254)
    val email: String,
    @field:NotBlank
    @field:Size(min = 12, max = 128)
    val password: String,
    @field:AssertTrue(message = "Privacy policy must be accepted")
    val privacyPolicyAccepted: Boolean,
    @field:AssertTrue(message = "Terms of service must be accepted")
    val termsOfServiceAccepted: Boolean,
)
