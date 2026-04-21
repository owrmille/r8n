package com.r8n.backend.mock.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(schema = "mock", name = "support_threads")
class SupportThreadPersistence(
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    var id: UUID? = null,
//
    @Column(name = "owner_user_id", nullable = false)
    var ownerUserId: UUID,
//
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
//
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)