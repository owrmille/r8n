package com.r8n.backend.users.api

import com.r8n.backend.users.api.dto.AccountDeletionRequestDto
import com.r8n.backend.users.api.dto.UpdateMyPublicProfileRequestDto
import com.r8n.backend.users.api.dto.UserProfileDto
import com.r8n.backend.users.api.dto.UsernameDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface UsersApi {
    companion object {
        private const val ROOT_PATH = "/api/users"
        const val ME_PATH = "$ROOT_PATH/me"
        const val USER_PATH = "$ROOT_PATH/{id}"
        const val MY_PUBLIC_PROFILE_PATH = "$ME_PATH/public-profile"
        const val MY_AVATAR_PATH = "$ME_PATH/avatar"
        const val USER_AVATAR_PATH = "$USER_PATH/avatar"
        const val DELETE_ACCOUNT_PATH = "$ME_PATH/delete"
    }

    @GetMapping(ME_PATH)
    fun getMyName(): UsernameDto

    @GetMapping(USER_PATH)
    fun getUserProfile(
        @PathVariable
        id: UUID,
    ): UserProfileDto

    @PatchMapping(MY_PUBLIC_PROFILE_PATH)
    fun updateMyPublicProfile(
        @RequestBody
        request: UpdateMyPublicProfileRequestDto,
    ): UserProfileDto

    @GetMapping(USER_AVATAR_PATH)
    fun getUserAvatar(
        @PathVariable
        id: UUID,
    ): ResponseEntity<ByteArray>

    @PostMapping(
        MY_AVATAR_PATH,
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun uploadMyAvatar(
        @RequestPart("file")
        file: MultipartFile,
    ): ResponseEntity<Void>

    @DeleteMapping(MY_AVATAR_PATH)
    fun deleteMyAvatar(): ResponseEntity<Void>

    @PostMapping(DELETE_ACCOUNT_PATH)
    fun requestAccountDeletion(
        @RequestBody
        request: AccountDeletionRequestDto,
    ): ResponseEntity<Void>
}
