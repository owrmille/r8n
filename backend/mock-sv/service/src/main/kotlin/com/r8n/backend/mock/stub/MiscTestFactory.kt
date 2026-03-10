package com.r8n.backend.mock.stub

import com.r8n.backend.mock.api.dto.SupportThreadDto
import java.util.UUID

object MiscTestFactory {
    fun getSupportMessage() = SupportThreadDto(
        UUID.randomUUID(),
        listOf("can I speak to the manager? MfG Karen", "yes Karen, how can I help you today? MfG John"),
    )
}