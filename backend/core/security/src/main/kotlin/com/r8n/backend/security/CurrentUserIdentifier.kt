package com.r8n.backend.security

import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

object CurrentUserIdentifier {
    fun getCurrentUserId(): UUID {
        val auth = SecurityContextHolder.getContext().authentication ?: throw IllegalStateException("Not authenticated")
        return UUID.fromString(auth.name)
    }
}