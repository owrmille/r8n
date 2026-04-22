package com.r8n.backend.opinions.lists.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.opinions.lists.facade.OpinionListFacade
import com.r8n.backend.security.Authority.IS_USER_OR_SERVICE
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class OpinionListInternalController(
    private val opinionListFacade: OpinionListFacade,
) : OpinionListsInternalApi {
    @PreAuthorize(IS_USER_OR_SERVICE)
    override fun getMineFull(pageable: PageRequestDto): PageResponseDto<OpinionListDto> =
        opinionListFacade.getListsFull(getCurrentUserId(), pageable)
}