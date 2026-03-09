package com.r8n.backend.mock.api

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.dto.SupportThreadDto
import com.r8n.backend.mock.api.dto.about.SelectorDto
import java.util.UUID

interface SelectorApi {
    fun getForURL(url: String, pageable: PageRequestDto): PageResponseDto<SelectorDto>
    fun getForSubject(id: UUID, pageable: PageRequestDto): PageResponseDto<SelectorDto>
    fun suggest(subjectId: UUID, selector: String): SelectorDto
    fun disagree(selectorId: UUID, comment: String?): SupportThreadDto
}