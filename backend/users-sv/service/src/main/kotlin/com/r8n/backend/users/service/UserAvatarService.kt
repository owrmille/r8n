package com.r8n.backend.users.service

import com.r8n.backend.users.persistence.ProfileAvatarPersistence
import com.r8n.backend.users.provider.database.ProfileAvatarRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.NoSuchFileException
import java.time.Instant
import java.util.UUID

@Service
class UserAvatarService(
    private val profileAvatarRepository: ProfileAvatarRepository,
    private val avatarStorage: AvatarStorage,
    @Value("\${r8n.storage.avatar.max-size}") private val maxSize: DataSize,
) {
    private companion object {
        val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/png", "image/webp")
    }

    @Transactional
    fun uploadAvatar(
        userId: UUID,
        file: MultipartFile,
    ) {
        val content = validateAvatarFile(file)
        val previousAvatar = profileAvatarRepository.findByIdOrNull(userId)
        val storedAvatar = avatarStorage.store(userId, content)

        try {
            profileAvatarRepository.save(
                ProfileAvatarPersistence(
                    userId = userId,
                    storageBackend = storedAvatar.storageBackend,
                    objectKey = storedAvatar.objectKey,
                    contentType = file.contentType!!,
                    fileSize = content.size.toLong(),
                    updatedAt = Instant.now(),
                ),
            )
        } catch (e: RuntimeException) {
            avatarStorage.delete(storedAvatar.objectKey)
            throw e
        }

        previousAvatar
            ?.objectKey
            ?.takeIf { it != storedAvatar.objectKey }
            ?.let { avatarStorage.delete(it) }
    }

    fun getAvatar(userId: UUID): UserAvatarFile {
        val avatar =
            profileAvatarRepository.findByIdOrNull(userId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found")

        val content =
            try {
                avatarStorage.load(avatar.objectKey)
            } catch (e: NoSuchFileException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found", e)
            }

        return UserAvatarFile(
            content = content,
            contentType = avatar.contentType,
        )
    }

    @Transactional
    fun deleteAvatar(userId: UUID) {
        val avatar = profileAvatarRepository.findByIdOrNull(userId) ?: return

        profileAvatarRepository.delete(avatar)
        avatarStorage.delete(avatar.objectKey)
    }

    private fun validateAvatarFile(file: MultipartFile): ByteArray {
        if (file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is empty")
        }

        if (file.size > maxSize.toBytes()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is too large")
        }

        val contentType =
            file.contentType
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar content type is missing")

        if (contentType !in ALLOWED_CONTENT_TYPES) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar content type is not supported")
        }

        val content = file.bytes
        if (!content.matchesContentType(contentType)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file content does not match its content type")
        }

        return content
    }

    private fun ByteArray.matchesContentType(contentType: String): Boolean =
        when (contentType) {
            "image/jpeg" -> isJpeg()
            "image/png" -> isPng()
            "image/webp" -> isWebp()
            else -> false
        }

    private fun ByteArray.isJpeg(): Boolean =
        size >= 3 &&
            this[0] == 0xFF.toByte() &&
            this[1] == 0xD8.toByte() &&
            this[2] == 0xFF.toByte()

    private fun ByteArray.isPng(): Boolean {
        val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        return size >= signature.size && copyOfRange(0, signature.size).contentEquals(signature)
    }

    private fun ByteArray.isWebp(): Boolean =
        size >= 12 &&
            decodeToString(0, 4) == "RIFF" &&
            decodeToString(8, 12) == "WEBP"
}

data class UserAvatarFile(
    val content: ByteArray,
    val contentType: String,
)