package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.OpinionListDto
import java.util.UUID

interface OpinionListApi {
    fun getOpinionListDescription(id: UUID): OpinionListDto
    
}