package com.r8n.backend.opinions.api.dto.about

import java.util.UUID

data class SelectorDto(
    val id: UUID,
    val referentId: UUID,
    val urlRegex: String,
    val urlHumanReadable: String,
    val selector: String,
)