package com.r8n.backend.opinions.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(schema = "users", name = "outbox")
class OutboxEvent(
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    val id: UUID? = null,
//
    @Column(nullable = false)
    val aggregateType: String,
//
    @Column(nullable = false)
    val aggregateId: String,
//
    @Column(nullable = false)
    val type: String,
//
    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,
//
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
//
    @Column
    var publishedAt: Instant? = null,
)