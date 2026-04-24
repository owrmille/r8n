package com.r8n.backend.opinions.lists.facade

import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListPrivacyEnumDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionSummaryDto
import com.r8n.backend.opinions.lists.domain.OpinionList
import com.r8n.backend.opinions.lists.domain.OpinionListPrivacyEnum
import com.r8n.backend.opinions.lists.domain.OpinionSummary
import com.r8n.backend.opinions.opinions.facade.OpinionMapper
import com.r8n.backend.opinions.opinions.service.SubjectService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.springframework.stereotype.Component

@Component
class OpinionListMapper(
    private val usersClient: UsersInternalApi,
    private val subjectService: SubjectService,
    private val opinionMapper: OpinionMapper,
) {
    fun toDto(opinionList: OpinionList): OpinionListDto =
        with(opinionList) {
            OpinionListDto(
                id = id,
                listName = name,
                owner = owner,
                ownerName = usersClient.getUserName(owner),
                opinionSummaries = opinionSummaries.map { toDto(it) },
                privacy = privacy.toDto(),
            )
        }

    fun toDto(opinionSummary: OpinionSummary) =
        with(opinionSummary) {
            OpinionSummaryDto(
                subject = subject,
                subjectName = subjectService.getSubjectName(subject) ?: "UNNAMED",
                ownMark = ownMark,
                componentMark = componentMark,
                opinions = opinions.map { opinionMapper.toDto(it) },
            )
        }

    private companion object {
        fun OpinionListPrivacyEnum.toDto() =
            when (this) {
                OpinionListPrivacyEnum.SEARCHABLE -> OpinionListPrivacyEnumDto.SEARCHABLE
                OpinionListPrivacyEnum.PRIVATE -> OpinionListPrivacyEnumDto.PRIVATE
            }
    }
}