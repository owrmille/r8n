package com.r8n.backend.mock.integration

import java.util.UUID

interface UserClient {
    fun getUserName(id: UUID): String
}
