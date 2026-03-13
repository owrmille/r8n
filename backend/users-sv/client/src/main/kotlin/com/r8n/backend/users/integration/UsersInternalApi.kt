package com.r8n.backend.users.integration

import java.util.UUID

interface UsersInternalApi {
    fun getUserName(id: UUID): String
}
