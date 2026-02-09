package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.about.SelectorDto
import java.util.UUID

interface SelectorApi {
    fun getForURL(url: String): List<SelectorDto>
    fun suggest(subjectId: UUID, selector: String): SelectorDto
    fun disagree(id: UUID)
}