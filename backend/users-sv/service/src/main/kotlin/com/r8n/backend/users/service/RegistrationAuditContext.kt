package com.r8n.backend.users.service

data class RegistrationAuditContext(
    val ip: String,
    val userAgent: String,
)

data class LoginAuditContext(
    val ip: String,
    val userAgent: String,
)
