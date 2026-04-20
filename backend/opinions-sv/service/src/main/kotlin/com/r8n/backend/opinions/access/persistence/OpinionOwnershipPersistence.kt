package com.r8n.backend.opinions.access.controller.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(schema = "access", name = "opinion_ownership")
class OpinionOwnershipPersistence(
    @Id
    val opinionId: UUID,
//
    @Column(nullable = false)
    val ownerId: UUID,
)