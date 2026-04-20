package com.r8n.backend.opinions.lists.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(schema = "opinion_lists", name = "opinions_to_lists")
class OpinionListOpinionAssignmentPersistence(
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    var id: UUID? = null,
//
    @Column(nullable = false)
    var opinionListId: UUID,
//
    @Column(nullable = false)
    var opinionId: UUID,
)