package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.UserCompleteDataDto
import org.springframework.web.bind.annotation.GetMapping
import java.util.UUID

interface UsersApi {
    @GetMapping("/users/export")
    fun exportAll(userId: UUID): UserCompleteDataDto
}