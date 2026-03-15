package com.r8n.backend.users.controller

import com.r8n.backend.users.api.UsersApi
import com.r8n.backend.users.api.dto.UserCompleteDataDto
import com.r8n.backend.users.facade.UserFacade
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class UserController(
    private val userFacade: UserFacade,
) : UsersApi {

    fun exportAll(
        @AuthenticationPrincipal jwt: Jwt
    ) = exportAll(UUID.fromString(jwt.subject))

    override fun exportAll(userId: UUID): UserCompleteDataDto = userFacade.getUserCompleteDataDto(userId)
}