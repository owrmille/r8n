package com.r8n.backend.opinions.opinions.service

import com.r8n.backend.opinions.access.domain.OpinionPermissionEnum
import com.r8n.backend.opinions.access.service.AccessService
import com.r8n.backend.opinions.opinions.database.OpinionRepository
import com.r8n.backend.opinions.opinions.domain.Opinion
import com.r8n.backend.opinions.opinions.domain.OpinionStatusEnum
import com.r8n.backend.opinions.opinions.persistence.OpinionPersistence
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
    private val accessService: AccessService,
) {
    fun getOpinion(
        id: UUID,
        requesterId: UUID,
    ): Opinion {
        val opinion = opinionRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.canAccessOpinion(requesterId, opinion.id!!, OpinionPermissionEnum.READ)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        return opinion.toDomain()
    }

    fun getOpinionFor(
        subjectId: UUID,
        requesterId: UUID,
    ): Opinion {
        val opinion =
            opinionRepository.findFirstBySubjectAndOwnerOrderByTimestampDesc(subjectId, requesterId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return opinion.toDomain()
    }

    @Transactional
    fun createOpinion(
        subjectId: UUID,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
        creatorId: UUID,
    ): Opinion {
        val opinion =
            opinionRepository.save(
                OpinionPersistence(
                    owner = creatorId,
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
        requesterId: UUID,
    ): Opinion {
        val opinion =
            opinionRepository
                .findById(
                    opinionId,
                ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.canAccessOpinion(requesterId, opinion.id!!, OpinionPermissionEnum.EDIT)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        opinion.mark = mark
        opinion.timestamp = Instant.now()
        val savedOpinion = opinionRepository.save(opinion)
        noteService.replace(savedOpinion.id!!, subjective, objective)
        return savedOpinion.toDomain()
    }

    @Transactional
    fun deleteOpinion(
        opinionId: UUID,
        requesterId: UUID,
    ) {
        val opinion =
            opinionRepository
                .findById(
                    opinionId,
                ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.canAccessOpinion(requesterId, opinion.id!!, OpinionPermissionEnum.DELETE)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        opinionRepository.delete(opinion)
    }

    @Transactional
    fun linkComponent(
        parentOpinionId: UUID,
        childOpinionId: UUID,
        weight: Double,
        requesterId: UUID,
    ): Opinion {
        if (parentOpinionId == childOpinionId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "An opinion cannot be linked to itself")
        }

        opinionRepository
            .findById(parentOpinionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.canAccessOpinion(requesterId, parentOpinionId, OpinionPermissionEnum.EDIT)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        opinionRepository
            .findById(childOpinionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.canAccessOpinion(requesterId, childOpinionId, OpinionPermissionEnum.READ)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        if (componentService.hasLink(parentOpinionId, childOpinionId)) {
            return getOpinion(parentOpinionId, requesterId)
        }
        try {
            componentService.linkComponent(parentOpinionId, childOpinionId, weight)
        } catch (_: DataIntegrityViolationException) {
            // Another concurrent request inserted the same link first: keep operation idempotent.
        }
        return getOpinion(parentOpinionId, requesterId)
    }

    @Transactional
    fun unlinkComponent(
        linkId: UUID,
        requesterId: UUID,
    ): Opinion {
        val parentOpinionId =
            componentService.getParentOpinionId(linkId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        opinionRepository
            .findById(parentOpinionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.canAccessOpinion(requesterId, parentOpinionId, OpinionPermissionEnum.EDIT)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        componentService.unlinkComponent(linkId)
        return getOpinion(parentOpinionId, requesterId)
    }

    @Transactional
    fun adjustComponentWeight(
        linkId: UUID,
        weight: Double,
        requesterId: UUID,
    ): Opinion {
        val parentOpinionId =
            componentService.getParentOpinionId(linkId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        opinionRepository
            .findById(parentOpinionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.canAccessOpinion(requesterId, parentOpinionId, OpinionPermissionEnum.EDIT)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        if (!componentService.adjustComponentWeight(linkId, weight)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
        return getOpinion(parentOpinionId, requesterId)
    }

    private fun OpinionPersistence.toDomain(): Opinion =
        Opinion(
            id!!,
            owner,
            subject,
            subjectService.getSubjectName(subject) ?: "UNNAMED",
            noteService.getSubjective(id!!),
            noteService.getObjective(id!!),
            mark,
            componentService.getComponentSection(id!!),
            status,
            timestamp,
        )
}