package com.r8n.backend.mock.api.dto.about

import java.util.UUID

data class ReferentDto(
    val id: UUID,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
)