package com.r8n.backend.opinionlists.kafka

import com.r8n.backend.opinionlists.provider.database.OutboxRepository
import jakarta.transaction.Transactional
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OutboxPublisher(
    private val repo: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun publish() {
        val events = repo.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()

        for (event in events) {
            kafkaTemplate
                .send(
                    "opinion-list-events",
                    event.aggregateId,
                    event.payload,
                ).get()

            event.publishedAt = Instant.now()
        }
    }
}