package com.r8n.backend.users.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(schema = "users", name = "pii")
class PIIPersistence(
    @Id
    @GeneratedValue
    @UuidGenerator
    val userId: UUID,
//
    @Column(nullable = false)
    val name: String,
//
    @Column(nullable = true)
    val email: String,
//
    @Column(nullable = true)
    val phone: String,
)