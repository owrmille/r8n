package com.r8n.backend.users.api.dto

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val privacyPolicyAccepted: Boolean,
    val termsOfServiceAccepted: Boolean,
)
