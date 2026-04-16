package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.domain.Opinion
import com.r8n.backend.opinions.domain.OpinionStatusEnum
import com.r8n.backend.opinions.persistence.OpinionPersistence
import com.r8n.backend.opinions.provider.database.OpinionRepository
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class OpinionService(
    private val subjectService: SubjectService,
    private val noteService: OpinionNoteService,
    private val componentService: ComponentService,
    private val opinionRepository: OpinionRepository,
) {
    fun getOpinion(id: UUID): Opinion {
        val opinion = opinionRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!getCurrentUserId().isOwnerOf(opinion)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        return opinion.toDomain()
    }

    private fun UUID.isOwnerOf(opinion: OpinionPersistence): Boolean = this == opinion.owner

    fun getOpinionFor(subjectId: UUID): Opinion {
        val opinion =
            opinionRepository.findFirstBySubjectOrderByTimestampDesc(subjectId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        if (opinion.owner != getCurrentUserId()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        return opinion.toDomain()
    }

    @Transactional
    fun createOpinion(
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): Opinion {
        val opinion =
            opinionRepository.save(
                OpinionPersistence(
                    owner = getCurrentUserId(),
                    subject = subjectId,
                    mark = mark,
                    status = OpinionStatusEnum.DRAFT,
                    timestamp = Instant.now(),
                ),
            )
        noteService.create(opinion.id!!, subjective, objective)
        return opinion.toDomain()
    }

    @Transactional
    fun updateOpinion(
        opinionId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): Opinion {
        val opinion =
            opinionRepository
                .findById(
                    opinionId,
                ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (opinion.owner != getCurrentUserId()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        opinion.mark = mark
        opinion.timestamp = Instant.now()
        val savedOpinion = opinionRepository.save(opinion)
        noteService.replace(savedOpinion.id!!, subjective, objective)
        return savedOpinion.toDomain()
    }

    @Transactional
    fun deleteOpinion(opinionId: UUID) {
        val opinion =
            opinionRepository
                .findById(
                    opinionId,
                ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (opinion.owner != getCurrentUserId()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        opinionRepository.delete(opinion)
    }

    private fun OpinionPersistence.toDomain(): Opinion =
        Opinion(
            id!!,
            owner,
            subject,
            subjectService.getSubjectName(subject),
            noteService.getSubjective(id!!),
            noteService.getObjective(id!!),
            mark,
            componentService.getComponentSection(id!!),
            status,
            timestamp,
        )
}