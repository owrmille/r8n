package com.r8n.backend.messaging.provider.database

import com.r8n.backend.messaging.persistence.ConversationPersistence
import com.r8n.backend.messaging.persistence.ConversationTypeEnumPersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ConversationRepository : JpaRepository<ConversationPersistence, UUID> {
    fun findAllByTypeOrderByLastMessageAtDesc(
        type: ConversationTypeEnumPersistence,
        pageable: Pageable,
    ): Page<ConversationPersistence>
}
