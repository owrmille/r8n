package com.r8n.backend.users.facade

import com.r8n.backend.users.api.dto.UserCompleteDataDto
import com.r8n.backend.users.service.UsersService
import org.springframework.stereotype.Component

@Component
class UsersFacade(
    private val usersService: UsersService,
) {
    fun getUserCompleteDataDto(): UserCompleteDataDto {
        val res = UserCompleteDataDto(

        )
    }
}