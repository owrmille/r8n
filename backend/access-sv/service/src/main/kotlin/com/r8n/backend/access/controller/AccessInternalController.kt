package com.r8n.backend.access.controller

import com.r8n.backend.access.facade.AccessRequestFacade
import com.r8n.backend.access.integration.api.AccessInternalApi
import com.r8n.backend.access.integration.api.PermissionEnumDto
import com.r8n.backend.security.Authority.IS_SERVICE
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class AccessInternalController(
    private val facade: AccessRequestFacade,
) : AccessInternalApi {
    @PreAuthorize(IS_SERVICE)
    override fun canAccessOpinion(
        permission: PermissionEnumDto,
        opinionId: UUID,
    ): Boolean = facade.canAccessOpinion(getCurrentUserId(), opinionId, permission)

    @PreAuthorize(IS_SERVICE)
    override fun canAccessOpinionList(
        permission: PermissionEnumDto,
        opinionListId: UUID,
    ): Boolean = facade.canAccessOpinionList(getCurrentUserId(), opinionListId, permission)
}