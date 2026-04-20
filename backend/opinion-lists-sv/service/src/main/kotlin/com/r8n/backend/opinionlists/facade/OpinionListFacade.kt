package com.r8n.backend.opinionlists.facade

import com.r8n.backend.opinionlists.api.dto.OpinionListDto
import java.util.UUID

class OpinionListFacade(
    private val opinionListService: OpinionListService,
) {
    fun getList(listId: UUID): OpinionListDto {
        return opinionListService.getList(listId).toDto()
    }

    private companion object {

    }
}