package com.r8n.backend.mock.client

import com.r8n.backend.mock.api.dto.users.UserSummaryDto
import java.util.UUID

interface UserClient {
    fun getUserSummary(id: UUID): UserSummaryDto
}
