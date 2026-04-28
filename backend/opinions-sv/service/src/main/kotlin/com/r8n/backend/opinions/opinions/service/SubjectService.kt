package com.r8n.backend.opinions.opinions.service

import com.r8n.backend.opinions.opinions.database.OpinionSubjectRepository
import com.r8n.backend.opinions.opinions.database.ReferentRepository
import com.r8n.backend.opinions.opinions.domain.SubjectDetails
import com.r8n.backend.opinions.opinions.domain.SubjectReferent
import com.r8n.backend.opinions.opinions.persistence.OpinionSubjectPersistence
import com.r8n.backend.opinions.opinions.persistence.ReferentPersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

// no permissions checks needed, all subjects are available to all users
@Service
class SubjectService(
    private val opinionSubjectRepository: OpinionSubjectRepository,
    private val referentRepository: ReferentRepository,
) {
    fun getSubjectName(id: UUID): String? = getSubject(id)?.name

    @Transactional(readOnly = true)
    fun findSubjects(
        query: String,
        pageable: Pageable,
    ): Page<SubjectDetails> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "query must not be blank")
        }

        return opinionSubjectRepository
            .findByNameContainingIgnoreCaseOrderByNameAsc(trimmedQuery, pageable)
            .map { it.toModel() }
    }

    @Transactional
    fun createSubject(
        name: String,
        referentName: String?,
        address: String?,
        latitude: Double?,
        longitude: Double?,
    ): SubjectDetails {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }

        val savedReferent =
            referentRepository.save(
                ReferentPersistence(
                    name = referentName.trimToNull() ?: trimmedName,
                    address = address.trimToNull(),
                    latitude = latitude,
                    longitude = longitude,
                    referentGroup = UUID.randomUUID(),
                ),
            )
        val savedSubject =
            opinionSubjectRepository.save(
                OpinionSubjectPersistence(
                    name = trimmedName,
                    referent = savedReferent.id!!,
                ),
            )

        return savedSubject.toModel()
    }

    @Transactional(readOnly = true)
    fun getSubject(id: UUID): SubjectDetails? {
        val subject = opinionSubjectRepository.findById(id).orElse(null) ?: return null
        return subject.toModel()
    }

    private fun OpinionSubjectPersistence.toModel(): SubjectDetails {
        val primaryReferent =
            referentRepository.findById(referent).orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                )
            }
        val alternativeReferents =
            referentRepository
                .findAllByReferentGroupOrderByIdAsc(primaryReferent.referentGroup)
                .filter { it.id != primaryReferent.id }
                .map { it.toModel() }

        return SubjectDetails(
            id = id!!,
            name = name,
            primaryReferent = primaryReferent.toModel(),
            alternativeReferents = alternativeReferents,
        )
    }

    private fun String?.trimToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun ReferentPersistence.toModel() =
        SubjectReferent(
            id = id!!,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
        )
}
