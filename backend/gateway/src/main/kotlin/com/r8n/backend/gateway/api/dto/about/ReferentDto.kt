package com.r8n.backend.gateway.api.dto.about

import java.util.UUID

class ReferentDto(
    val id: UUID,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
)