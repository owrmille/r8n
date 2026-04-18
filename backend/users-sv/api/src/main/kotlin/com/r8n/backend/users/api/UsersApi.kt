package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.UserCompleteDataDto
import org.springframework.web.bind.annotation.GetMapping

interface UsersApi {
    companion object {
        private const val ROOT_PATH = "/api/users"
        const val EXPORT_PATH = "$ROOT_PATH/export"
    }

    @GetMapping(EXPORT_PATH)
    fun exportAll(): UserCompleteDataDto
}