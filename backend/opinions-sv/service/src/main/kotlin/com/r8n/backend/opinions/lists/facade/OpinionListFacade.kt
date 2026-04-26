package com.r8n.backend.opinions.lists.facade

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.core.utils.toPageable
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.lists.service.OpinionListService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OpinionListFacade(
    private val opinionListService: OpinionListService,
    private val opinionListMapper: OpinionListMapper,
) {
    fun getList(
        listId: UUID,
        requesterId: UUID,
    ): OpinionListDto = opinionListMapper.toDto(opinionListService.getList(listId, requesterId))

    fun getListsFull(
        ownerId: UUID,
        pageable: PageRequestDto,
    ): PageResponseDto<OpinionListDto> =
        opinionListService
            .getListsFull(ownerId, pageable.toPageable())
            .map { opinionListMapper.toDto(it) }
            .toResponse()

    fun deleteAllUserDataForUser(userId: UUID) {
        opinionListService.deleteAllUserDataForUser(userId)
    }
}
}
