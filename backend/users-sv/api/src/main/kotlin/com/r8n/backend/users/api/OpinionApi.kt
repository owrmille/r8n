package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.UserCompleteDataDto

interface UsersApi {
    fun exportAll(): UserCompleteDataDto
}