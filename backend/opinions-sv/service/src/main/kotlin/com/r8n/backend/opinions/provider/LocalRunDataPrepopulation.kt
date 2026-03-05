package com.r8n.backend.opinions.provider

import com.r8n.backend.opinions.domain.fromDto
import com.r8n.backend.opinions.persistence.OpinionPersistence
import com.r8n.backend.opinions.provider.database.OpinionRepository
import com.r8n.backend.opinions.stub.OpinionTestDataFactory.alexanderOnDonald
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local")
@Component
class LocalRunDataPrepopulation(
    private val repo: OpinionRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val op = alexanderOnDonald()
        repo.saveAndFlush(
            OpinionPersistence(
                owner = op.owner,
                subject = op.subject,
                mark = op.mark,
                status = op.status.fromDto(),
                timestamp = op.timestamp,
            ),
        )
    }
}