package com.r8n.backend.access.integration.api

import org.springframework.web.bind.annotation.GetMapping
import java.util.UUID

interface AccessInternalApi {
    companion object {
        const val ACCESS_PATH = "/access"
        const val ACCESS_OPINION_PATH = "${ACCESS_PATH}/opinions/{id}"
        const val ACCESS_OPINION_LIST_PATH = "${ACCESS_PATH}/opinion-lists/{id}"
    }

    @GetMapping(ACCESS_OPINION_PATH)
    fun canAccessOpinion(
        permission: PermissionEnumDto,
        opinionId: UUID,
    ): Boolean

    @GetMapping(ACCESS_OPINION_LIST_PATH)
    fun canAccessOpinionList(
        permission: PermissionEnumDto,
        opinionListId: UUID,
    ): Boolean
}