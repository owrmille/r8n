package com.r8n.backend.opinions.lists.facade

import com.r8n.backend.opinions.lists.domain.OpinionList
import com.r8n.backend.opinions.lists.service.OpinionListService
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.users.integration.api.UsersInternalApi
import java.util.UUID

class OpinionListFacade(
    private val opinionListService: OpinionListService,
    private val usersClient: UsersInternalApi,
) {
    fun getList(listId: UUID): OpinionListDto {
        return opinionListService.getList(listId).toDto()
    }

    private companion object {
        fun OpinionList.toDto(): OpinionListDto = OpinionListDto(
            id = id,
            listName = name,
            owner = owner,
            ownerName = usersClient.getUserName(owner),

        )
    }
}