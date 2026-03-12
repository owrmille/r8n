package com.r8n.backend.users.provider.database

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UsersRepository : JpaRepository<UsersPersistence, UUID> {
}