package com.r8n.backend.users.integration

import java.util.UUID

interface UserClient {
    fun getUserName(id: UUID): String
}
