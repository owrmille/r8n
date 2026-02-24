package com.r8n.backend.gateway.stub

import com.r8n.backend.gateway.api.dto.SupportThreadDto
import java.util.UUID

object MiscTestFactory {
    fun getSupportMessage() = SupportThreadDto(
        UUID.randomUUID(),
        listOf("can I speak to the manager? MfG Karen", "yes Karen, how can I help you today? MfG John"),
    )
}