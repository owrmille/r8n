package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.opinion.OpinionDto
import com.r8n.backend.gateway.api.dto.list.OpinionListDto
import com.r8n.backend.gateway.api.dto.list.OpinionListPrivacyEnumDto
import com.r8n.backend.gateway.api.dto.list.OpinionListSummaryDto
import java.util.UUID

interface OpinionListApi {
    fun getListSummary(id: UUID): OpinionListSummaryDto
    fun getList(id: UUID): OpinionListDto
    fun renameList(id: UUID, name: String): OpinionListDto
    fun changePrivacy(id: UUID, privacy: OpinionListPrivacyEnumDto): OpinionListDto
    fun linkOpinion(listId: UUID, opinionId: UUID): OpinionListDto
    fun unlinkOpinion(listId: UUID, opinionId: UUID): OpinionListDto
    fun searchByName(name: String): List<OpinionDto>
    fun searchByOwner(owner: UUID): List<OpinionDto>
}
