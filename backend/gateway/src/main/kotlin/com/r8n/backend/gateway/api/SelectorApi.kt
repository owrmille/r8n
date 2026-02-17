package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.PageResponse
import com.r8n.backend.gateway.api.dto.SupportThreadDto
import com.r8n.backend.gateway.api.dto.about.SelectorDto
import org.springframework.data.domain.Pageable
import java.util.UUID

interface SelectorApi {
    fun getForURL(url: String, pageable: Pageable): PageResponse<SelectorDto>
    fun getForSubject(id: UUID, pageable: Pageable): PageResponse<SelectorDto>
    fun suggest(subjectId: UUID, selector: String): SelectorDto
    fun disagree(selectorId: UUID, comment: String?): SupportThreadDto
}