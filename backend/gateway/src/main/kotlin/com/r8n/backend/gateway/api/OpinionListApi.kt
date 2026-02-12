package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.list.OpinionListDto
import com.r8n.backend.gateway.api.dto.list.OpinionListPrivacyEnumDto
import com.r8n.backend.gateway.api.dto.list.OpinionListSummaryDto
import java.util.UUID

interface OpinionListApi {
    fun getListSummary(listId: UUID): OpinionListSummaryDto
    fun getList(listId: UUID): OpinionListDto
    fun renameList(listId: UUID, name: String): OpinionListDto
    fun changePrivacy(listId: UUID, privacy: OpinionListPrivacyEnumDto): OpinionListDto
    fun linkOpinion(listId: UUID, opinionId: UUID): OpinionListDto
    fun unlinkOpinion(listId: UUID, opinionId: UUID): OpinionListDto
    fun searchByName(name: String): List<OpinionListSummaryDto>
    fun searchByOwner(owner: UUID): List<OpinionListSummaryDto>
}
