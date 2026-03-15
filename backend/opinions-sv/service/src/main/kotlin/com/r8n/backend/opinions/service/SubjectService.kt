package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.api.dto.about.OpinionSubjectDto
import com.r8n.backend.opinions.api.dto.about.ReferentDto
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
    fun getSubjectName(id: UUID): String = getSubject(id)?.name ?: "Subject"

    fun getSubject(id: UUID): OpinionSubjectDto? {
        val subject = opinionSubjectRepository.findById(id).orElse(null) ?: return null
        val primaryReferent = referentRepository.findById(subject.referent).orElse(null)
        val alternativeReferents = primaryReferent
            ?.let { referentRepository.findAllByReferentGroupOrderByIdAsc(it.referentGroup) }
            .orEmpty()
            .filter { it.id != primaryReferent?.id }
            .map { it.toDto() }

        return OpinionSubjectDto(
            id = subject.id ?: return null,
            name = subject.name,
            primaryReferent = primaryReferent?.toDto(),
            alternativeReferents = alternativeReferents,
        )
    }

    private fun ReferentPersistence.toDto() = ReferentDto(
        id = id!!,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
    )
}
