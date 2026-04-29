package com.r8n.backend.opinions.opinions.controllerInternal

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import com.r8n.backend.opinions.integration.api.OpinionsInternalApi
import com.r8n.backend.opinions.opinions.facade.OpinionFacade
import com.r8n.backend.security.Authority
import com.r8n.backend.security.CurrentUserIdentifier
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class OpinionsInternalController(
    private val opinionFacade: OpinionFacade,
) : OpinionsInternalApi {
    @PreAuthorize(Authority.IS_USER_OR_SERVICE)
    override fun getMyFullOpinions(pageable: PageRequestDto): PageResponseDto<OpinionDto> =
        opinionFacade.getMyFullOpinions(CurrentUserIdentifier.getCurrentUserId(), pageable)
}
