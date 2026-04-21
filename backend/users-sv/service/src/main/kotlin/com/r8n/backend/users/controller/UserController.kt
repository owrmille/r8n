package com.r8n.backend.users.controller

import com.r8n.backend.security.Authority.IS_USER
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import com.r8n.backend.users.api.UsersApi
import com.r8n.backend.users.api.dto.UsernameDto
import com.r8n.backend.users.facade.UserFacade
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class UserController(
    private val userFacade: UserFacade,
) : UsersApi {
    @PreAuthorize(IS_USER)
    override fun getMyName(): UsernameDto = userFacade.getMyName(getCurrentUserId())

    @PreAuthorize(IS_USER)
    override fun getUserProfile(id: UUID) = userFacade.getUserProfile(id)
}