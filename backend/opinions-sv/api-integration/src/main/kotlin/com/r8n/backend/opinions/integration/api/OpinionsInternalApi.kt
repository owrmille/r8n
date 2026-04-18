package com.r8n.backend.opinions.integration.api

import org.springframework.web.bind.annotation.GetMapping
import java.util.UUID

interface OpinionsInternalApi {
    companion object {
        const val OPINION_OWNER_PATH = "/api/opinions/{opinionId}/owner"
    }

    @GetMapping(OPINION_OWNER_PATH)
    fun getOwnerOfOpinion(id: UUID): UUID
}