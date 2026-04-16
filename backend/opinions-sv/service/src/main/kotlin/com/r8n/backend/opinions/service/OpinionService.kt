package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.domain.Opinion
import com.r8n.backend.opinions.domain.OpinionStatusEnum
import com.r8n.backend.opinions.persistence.OpinionPersistence
import com.r8n.backend.opinions.provider.database.OpinionRepository
import org.springframework.dao.DataIntegrityViolationException
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
        return opinion.toDomain()
    }

    fun getOpinionFor(subjectId: UUID): Opinion {
        val opinion =
            opinionRepository.findFirstBySubjectOrderByTimestampDesc(subjectId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return opinion.toDomain()
    }

    @Transactional
    fun createOpinion(
        ownerId: UUID,
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ): Opinion {
        val opinion =
            opinionRepository.save(
                OpinionPersistence(
                    owner = ownerId,
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
        opinionRepository.delete(opinion)
    }

    @Transactional
    fun linkComponent(
        userId: UUID,
        parentOpinionId: UUID,
        childOpinionId: UUID,
        weight: Double,
    ): Opinion {
        if (parentOpinionId == childOpinionId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "An opinion cannot be linked to itself")
        }

        val parentOpinion =
            opinionRepository
                .findById(parentOpinionId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (parentOpinion.owner != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        if (!opinionRepository.existsById(childOpinionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
        if (componentService.hasLink(parentOpinionId, childOpinionId)) {
            return getOpinion(parentOpinionId)
        }
        try {
            componentService.linkComponent(parentOpinionId, childOpinionId, weight)
        } catch (_: DataIntegrityViolationException) {
            // Another concurrent request inserted the same link first: keep operation idempotent.
        }
        return getOpinion(parentOpinionId)
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