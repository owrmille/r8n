package com.r8n.backend.users.controller

import com.r8n.backend.users.api.UsersApi
import com.r8n.backend.users.facade.UsersFacade
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UsersController(
    private val usersFacade: UsersFacade,
) : UsersApi {

    @GetMapping("/export")
    override fun exportAll(
    ) = usersFacade.getUserCompleteDataDto()

}