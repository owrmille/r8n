package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.domain.Opinion
import com.r8n.backend.opinions.domain.fromDto
import com.r8n.backend.opinions.stub.OpinionTestDataFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OpinionService(
    private val subjectService: SubjectService,
    private val noteService: OpinionNoteService,
    private val componentService: ComponentService,
) {
    fun getOpinion(id: UUID): Opinion {
        val stub = OpinionTestDataFactory.getOpinion(id)
        return Opinion(
            stub.id,
            stub.owner,
            stub.subject,
            subjectService.getSubjectName(stub.subject),
            noteService.getSubjective(stub.id),
            noteService.getObjective(stub.id),
            stub.mark,
            componentService.getComponentSection(id),
            stub.status.fromDto(),
            stub.timestamp,
        )
    }
}