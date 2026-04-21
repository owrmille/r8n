package com.r8n.backend.users.controller

import com.r8n.backend.security.Authority.IS_USER
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import com.r8n.backend.users.api.UsersApi
import com.r8n.backend.users.api.dto.UserCompleteDataDto
import com.r8n.backend.users.api.dto.UsernameDto
import com.r8n.backend.users.facade.UserFacade
import com.r8n.backend.users.service.UserAvatarService
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
class UserController(
    private val userFacade: UserFacade,
    private val userAvatarService: UserAvatarService,
) : UsersApi {
    @PreAuthorize(IS_USER)
    override fun exportAll(): UserCompleteDataDto {
        val auth = SecurityContextHolder.getContext().authentication ?: throw IllegalStateException("Not authenticated")
        val userId = UUID.fromString(auth.name)
        return userFacade.getUserCompleteDataDto(userId)
    }

    @PreAuthorize(IS_USER)
    override fun getMyName(): UsernameDto = userFacade.getMyName()

    @PreAuthorize(IS_USER)
    override fun getUserProfile(id: UUID) = userFacade.getUserProfile(id)

    @PreAuthorize(IS_USER)
    override fun getUserAvatar(id: UUID): ResponseEntity<ByteArray> {
        val avatar = userAvatarService.getAvatar(id)
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(avatar.contentType))
            .cacheControl(CacheControl.noStore())
            .body(avatar.content)
    }

    @PreAuthorize(IS_USER)
    override fun uploadMyAvatar(file: MultipartFile): ResponseEntity<Void> {
        userAvatarService.uploadAvatar(getCurrentUserId(), file)
        return ResponseEntity.noContent().build()
    }

    @PreAuthorize(IS_USER)
    override fun deleteMyAvatar(): ResponseEntity<Void> {
        userAvatarService.deleteAvatar(getCurrentUserId())
        return ResponseEntity.noContent().build()
    }
}