package com.r8n.backend.opinions.lists.facade

import com.r8n.backend.opinions.lists.service.OpinionListService
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import java.util.UUID

class OpinionListFacade(
    private val opinionListService: OpinionListService,
    private val opinionListMapper: OpinionListMapper,
) {
    fun getList(listId: UUID): OpinionListDto {
        return opinionListMapper.toDto(opinionListService.getList(listId))
    }
}