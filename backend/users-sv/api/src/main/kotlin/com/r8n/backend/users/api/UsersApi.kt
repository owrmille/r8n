package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.UserCompleteDataDto
import com.r8n.backend.users.api.dto.UserProfileDto
import com.r8n.backend.users.api.dto.UsernameDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

interface UsersApi {
    companion object {
        private const val ROOT_PATH = "/api/users"
        const val EXPORT_PATH = "$ROOT_PATH/export"
        const val ME_PATH = "$ROOT_PATH/me"
        const val USER_PATH = "$ROOT_PATH/{id}"
    }

    @GetMapping(EXPORT_PATH)
    fun exportAll(): UserCompleteDataDto

    @GetMapping(ME_PATH)
    fun getMyName(): UsernameDto

    @GetMapping(USER_PATH)
    fun getUserProfile(
        @PathVariable
        id: UUID,
    ): UserProfileDto
}