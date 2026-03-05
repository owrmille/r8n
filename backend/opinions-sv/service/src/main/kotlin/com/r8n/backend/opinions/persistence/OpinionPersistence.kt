package com.r8n.backend.opinions.persistence

import com.r8n.backend.opinions.domain.OpinionStatusEnum
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "opinions")
open class OpinionPersistence(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,
    var owner: UUID,
    var subject: UUID,
    var mark: Double?,
    var status: OpinionStatusEnum,
    var timestamp: Instant,
) {
    @Suppress("unused") // used by Jakarta Persistence API
    protected constructor() : this(
        null,
        UUID.randomUUID(),
        UUID.randomUUID(),
        0.0,
        OpinionStatusEnum.DRAFT,
        Instant.now()
    )
}
