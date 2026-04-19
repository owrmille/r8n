package com.r8n.backend.opinions.controller

import com.r8n.backend.opinions.integration.api.OpinionsInternalApi
import com.r8n.backend.opinions.service.OpinionService
import com.r8n.backend.security.Authority.IS_SERVICE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class OpinionsInternalController(
    private val opinionService: OpinionService,
) : OpinionsInternalApi {
    @PreAuthorize(IS_SERVICE)
    override fun getOwnerOfOpinion(id: UUID) = opinionService.getOwnerOfOpinion(id)
}