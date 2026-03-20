package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.UserCompleteDataDto
import org.springframework.web.bind.annotation.GetMapping

interface UsersApi {
    @GetMapping("/users/export")
    fun exportAll(): UserCompleteDataDto
}