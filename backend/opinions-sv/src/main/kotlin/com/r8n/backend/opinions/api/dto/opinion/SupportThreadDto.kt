package com.r8n.backend.gateway.api.dto

import java.util.UUID

data class SupportThreadDto(
    val id: UUID,
    val messages: List<String>,
)