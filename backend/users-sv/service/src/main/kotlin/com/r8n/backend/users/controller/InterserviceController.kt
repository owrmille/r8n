package com.r8n.backend.users.controller

import com.r8n.backend.users.integration.UsersInternalApi
import com.r8n.backend.users.service.UsersService
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class InterserviceController(
    private val usersService: UsersService,
) : UsersInternalApi {
    override fun getUserName(id: UUID) = usersService.getName(id)
}