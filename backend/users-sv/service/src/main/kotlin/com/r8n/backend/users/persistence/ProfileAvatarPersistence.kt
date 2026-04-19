package com.r8n.backend.users.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(schema = "users", name = "profile_avatars")
class ProfileAvatarPersistence(
    @Id
    val userId: UUID,
//
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val storageBackend: AvatarStorageBackendPersistence,
//
    @Column(nullable = false)
    val objectKey: String,
//
    @Column(nullable = false)
    val contentType: String,
//
    @Column(nullable = false)
    val fileSize: Long,
//
    @Column(nullable = false)
    val updatedAt: Instant,
)

enum class AvatarStorageBackendPersistence {
    LOCAL,
}