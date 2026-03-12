package com.r8n.backend.users.controller

import com.r8n.backend.users.api.UsersApi
import com.r8n.backend.users.facade.UsersFacade
import com.r8n.backend.users.service.UsersService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/users")
class UsersController(
    private val usersFacade: UsersFacade,
    private val usersService: UsersService,
) : UsersApi {

    @GetMapping("/export")
    override fun exportAll(
        @AuthenticationPrincipal Jwt jwt
    ) = usersFacade.getUserCompleteDataDto(UUID.fromString(jwt.getSubject()))

    fun getName(id: UUID) = usersService.getName(id)
}