package com.r8n.backend.users.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UsersService {
    fun getName(id: UUID) = "username"
}