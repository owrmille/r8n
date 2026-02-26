package com.r8n.backend.mock.integration

import com.r8n.backend.mock.integration.dto.UserSummaryDto
import java.util.UUID

interface UserClient {
    fun getUserSummary(id: UUID): UserSummaryDto
}
