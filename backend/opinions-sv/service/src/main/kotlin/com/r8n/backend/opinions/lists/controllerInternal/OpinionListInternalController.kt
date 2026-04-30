package com.r8n.backend.opinions.lists.controllerInternal

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.integration.api.OpinionListsDeletionInternalApi
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.opinions.lists.facade.OpinionListFacade
import com.r8n.backend.security.Authority
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class OpinionListInternalController(
    private val opinionListFacade: OpinionListFacade,
) : OpinionListsInternalApi,
    OpinionListsDeletionInternalApi {
    @PreAuthorize(Authority.IS_USER_OR_SERVICE)
    override fun getMineFull(pageable: PageRequestDto): PageResponseDto<OpinionListDto> =
        opinionListFacade.getListsFull(getCurrentUserId(), pageable)

    @PreAuthorize(Authority.IS_SERVICE)
    override fun deleteAllUserDataForUser(userId: UUID) {
        opinionListFacade.deleteAllUserDataForUser(userId)
    }
}
