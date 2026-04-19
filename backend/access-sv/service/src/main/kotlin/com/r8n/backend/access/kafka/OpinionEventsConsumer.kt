package com.r8n.backend.access.kafka

import com.r8n.backend.access.persistence.OpinionOwnershipPersistence
import com.r8n.backend.access.persistence.OpinionOwnershipRepository
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Component
class OpinionEventsConsumer(
    private val objectMapper: ObjectMapper,
    private val accessRepo: OpinionOwnershipRepository,
) {

    @KafkaListener(topics = ["opinion-events"], groupId = "access-sv")
    fun handle(record: ConsumerRecord<String, String>) {
        val payload = record.value()

        val json = objectMapper.readTree(payload)
        val opinionId = UUID.fromString(json["opinionId"].asString())
        val ownerId = UUID.fromString(json["ownerId"].asString())

        handleOpinionCreated(opinionId, ownerId)
    }

    @Transactional
    fun handleOpinionCreated(opinionId: UUID, ownerId: UUID) {
        if (accessRepo.existsByOpinionId(opinionId)) {
            return
        }

        accessRepo.save(
            OpinionOwnershipPersistence(
                opinionId = opinionId,
                ownerId = ownerId
            )
        )
    }
}
