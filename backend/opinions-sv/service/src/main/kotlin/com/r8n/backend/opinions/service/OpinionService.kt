package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.domain.Opinion
import com.r8n.backend.opinions.provider.database.OpinionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
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
        return Opinion(
            opinion.id!!,
            opinion.owner,
            opinion.subject,
            subjectService.getSubjectName(opinion.subject),
            noteService.getSubjective(opinion.id!!),
            noteService.getObjective(opinion.id!!),
            opinion.mark,
            componentService.getComponentSection(id),
            opinion.status,
            opinion.timestamp,
        )
    }
}