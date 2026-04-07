package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.domain.SubjectDetails
import com.r8n.backend.opinions.domain.SubjectReferent
import com.r8n.backend.opinions.persistence.ReferentPersistence
import com.r8n.backend.opinions.provider.database.OpinionSubjectRepository
import com.r8n.backend.opinions.provider.database.ReferentRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubjectService(
    private val opinionSubjectRepository: OpinionSubjectRepository,
    private val referentRepository: ReferentRepository,
) {
    fun getSubjectName(id: UUID): String = getSubject(id)?.name ?: UNNAMED_SUBJECT

    fun getSubject(id: UUID): SubjectDetails? {
        val subject = opinionSubjectRepository.findById(id).orElse(null) ?: return null
        val primaryReferent = referentRepository.findById(subject.referent).orElseThrow()
        val alternativeReferents =
            referentRepository
                .findAllByReferentGroupOrderByIdAsc(primaryReferent.referentGroup)
                .filter { it.id != primaryReferent.id }
                .map { it.toModel() }

        return SubjectDetails(
            id = subject.id ?: return null,
            name = subject.name,
            primaryReferent = primaryReferent.toModel(),
            alternativeReferents = alternativeReferents,
        )
    }

    private fun ReferentPersistence.toModel() =
        SubjectReferent(
            id = id!!,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
        )

    companion object {
        private const val UNNAMED_SUBJECT = "UNNAMED"
    }
}