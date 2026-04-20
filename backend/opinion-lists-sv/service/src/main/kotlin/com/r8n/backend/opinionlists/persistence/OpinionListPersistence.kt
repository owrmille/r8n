package com.r8n.backend.opinionlists.persistence

import com.r8n.backend.opinionlists.domain.OpinionListPrivacyEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(schema = "opinion_lists", name = "opinion_lists")
class OpinionListPersistence(
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    var id: UUID? = null,
//
    @Column(nullable = false)
    var owner: UUID,
//
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var privacy: OpinionListPrivacyEnum,
)