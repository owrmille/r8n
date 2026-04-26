package com.r8n.backend.users.controller

import com.r8n.backend.security.Authority.IS_USER
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import com.r8n.backend.users.api.UsersApi
import com.r8n.backend.users.api.dto.AccountDeletionRequestDto
import com.r8n.backend.users.api.dto.UpdateMyPublicProfileRequestDto
import com.r8n.backend.users.api.dto.UsernameDto
import com.r8n.backend.users.facade.UserFacade
import com.r8n.backend.users.service.UserAvatarService
import com.r8n.backend.users.service.UserDeletionService
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
class UserController(
    private val userFacade: UserFacade,
    private val userAvatarService: UserAvatarService,
    private val userDeletionService: UserDeletionService,
) : UsersApi {
    @PreAuthorize(IS_USER)
    override fun getMyName(): UsernameDto = userFacade.getMyName(getCurrentUserId())

    @PreAuthorize(IS_USER)
    override fun getUserProfile(id: UUID) = userFacade.getUserProfile(id)

    @PreAuthorize(IS_USER)
    override fun updateMyPublicProfile(request: UpdateMyPublicProfileRequestDto) =
        userFacade.updateMyPublicProfile(getCurrentUserId(), request)

    @PreAuthorize(IS_USER)
    override fun getUserAvatar(id: UUID): ResponseEntity<ByteArray> {
        val avatar = userAvatarService.getAvatar(id) ?: return ResponseEntity.noContent().build()
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

    @PreAuthorize(IS_USER)
    override fun requestAccountDeletion(request: AccountDeletionRequestDto): ResponseEntity<Void> {
        val userId = getCurrentUserId()

        // Validate email confirmation
        if (!userDeletionService.validateEmailConfirmation(userId, request.email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        // Delete user data
        userDeletionService.deleteUser(userId)

        return ResponseEntity.noContent().build()
    }
}