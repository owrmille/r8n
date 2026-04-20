package com.r8n.backend.opinions.access.facade

import com.r8n.backend.opinions.api.access.dto.RequestStatusEnumDto
import com.r8n.backend.opinions.access.domain.AccessRequest
import com.r8n.backend.opinions.access.domain.OpinionListPermissionEnum
import com.r8n.backend.opinions.access.domain.OpinionPermissionEnum
import com.r8n.backend.opinions.access.domain.RequestStatusEnum
import com.r8n.backend.opinions.access.service.AccessRequestService
import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.api.access.dto.AccessRequestDto
import com.r8n.backend.opinions.opinions.service.OpinionService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AccessRequestFacade(
    private val service: AccessRequestService,
    private val usersInternalApi: UsersInternalApi,
    private val opinionService: OpinionService,
) {
    fun getIncoming(
        forListId: UUID?,
        since: Instant?,
        status: RequestStatusEnumDto?,
        pageable: PageRequestDto,
        ownerId: UUID,
    ): PageResponseDto<AccessRequestDto> {
        val pageRequest =
            PageRequest.of(
                pageable.page,
                pageable.size,
                Sort.by(pageable.sort.map { Sort.Order(Sort.Direction.valueOf(it.direction.name), it.property) }),
            )
        val page = service.getRequests(forListId, null, ownerId, status?.toDomain(), pageRequest)
        return PageResponseDto(
            items = page.content.map { it.toDto() },
            total = page.totalElements,
            page = pageable.page,
            size = pageable.size,
        )
    }

    fun getOutgoing(
        forListId: UUID?,
        since: Instant?,
        status: RequestStatusEnumDto?,
        pageable: PageRequestDto,
        requesterId: UUID,
    ): PageResponseDto<AccessRequestDto> {
        val pageRequest =
            PageRequest.of(
                pageable.page,
                pageable.size,
                Sort.by(pageable.sort.map { Sort.Order(Sort.Direction.valueOf(it.direction.name), it.property) }),
            )
        val page = service.getRequests(forListId, requesterId, null, status?.toDomain(), pageRequest)
        return PageResponseDto(
            items = page.content.map { it.toDto() },
            total = page.totalElements,
            page = pageable.page,
            size = pageable.size,
        )
    }

    fun createRequest(
        listId: UUID,
        requesterId: UUID,
    ): AccessRequestDto = service.createRequest(listId, requesterId).toDto()

    fun cancelRequest(
        requestId: UUID,
        requesterId: UUID,
    ): AccessRequestDto = service.cancelRequest(requestId, requesterId).toDto()

    fun acceptRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequestDto = service.acceptRequest(requestId, ownerId).toDto()

    fun declineRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequestDto = service.declineRequest(requestId, ownerId).toDto()

    fun hideRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequestDto = service.hideRequest(requestId, ownerId).toDto()

    private fun AccessRequest.toDto(): AccessRequestDto {
        val requesterName =
            try {
                usersInternalApi.getUserName(requesterId)
            } catch (_: Exception) {
                "Unknown"
            }
        val ownerName =
            try {
                usersInternalApi.getUserName(ownerId)
            } catch (_: Exception) {
                "Unknown"
            }
        val listName =
            try {
                opinionService.getListSummary(listId).listName
            } catch (_: Exception) {
                "Unknown"
            }

        return AccessRequestDto(
            id = id!!,
            opinionListId = listId,
            opinionListName = listName,
            owner = ownerId,
            ownerName = ownerName,
            requester = requesterId,
            requesterName = requesterName,
            timestamp = createdAt,
            status = status.toDto(),
        )
    }

    fun canAccessOpinion(
        userId: UUID,
        opinionId: UUID,
        permission: OpinionPermissionEnumDto,
    ): Boolean = service.canAccessOpinion(userId, opinionId, permission.toDomain())

    fun canAccessOpinionList(
        userId: UUID,
        opinionListId: UUID,
        permission: OpinionListPermissionEnumDto,
    ): Boolean = service.canAccessOpinionList(userId, opinionListId, permission.toDomain())

    private companion object {
        fun OpinionListPermissionEnumDto.toDomain() =
            when (this) {
                OpinionListPermissionEnumDto.VIEW -> OpinionListPermissionEnum.VIEW
                OpinionListPermissionEnumDto.ADD_TO -> OpinionListPermissionEnum.ADD_TO
                OpinionListPermissionEnumDto.REMOVE_FROM -> OpinionListPermissionEnum.REMOVE_FROM
                OpinionListPermissionEnumDto.HIDE -> OpinionListPermissionEnum.HIDE
                OpinionListPermissionEnumDto.PUBLISH -> OpinionListPermissionEnum.PUBLISH
                OpinionListPermissionEnumDto.DELETE -> OpinionListPermissionEnum.DELETE
            }

        fun OpinionPermissionEnumDto.toDomain() =
            when (this) {
                OpinionPermissionEnumDto.READ -> OpinionPermissionEnum.READ
                OpinionPermissionEnumDto.EDIT -> OpinionPermissionEnum.EDIT
                OpinionPermissionEnumDto.DELETE -> OpinionPermissionEnum.DELETE
                OpinionPermissionEnumDto.APPROVE -> OpinionPermissionEnum.APPROVE
                OpinionPermissionEnumDto.REJECT -> OpinionPermissionEnum.REJECT
            }

        fun OpinionPermissionEnum.toDto() =
            when (this) {
                OpinionPermissionEnum.READ -> OpinionPermissionEnumDto.READ
                OpinionPermissionEnum.EDIT -> OpinionPermissionEnumDto.EDIT
                OpinionPermissionEnum.DELETE -> OpinionPermissionEnumDto.DELETE
                OpinionPermissionEnum.APPROVE -> OpinionPermissionEnumDto.APPROVE
                OpinionPermissionEnum.REJECT -> OpinionPermissionEnumDto.REJECT
            }

        fun RequestStatusEnumDto.toDomain() =
            when (this) {
                RequestStatusEnumDto.SENT -> RequestStatusEnum.SENT
                RequestStatusEnumDto.ACCEPTED -> RequestStatusEnum.ACCEPTED
                RequestStatusEnumDto.REJECTED -> RequestStatusEnum.REJECTED
                RequestStatusEnumDto.HIDDEN -> RequestStatusEnum.HIDDEN
                RequestStatusEnumDto.CANCELLED -> RequestStatusEnum.CANCELLED
            }

        fun RequestStatusEnum.toDto() =
            when (this) {
                RequestStatusEnum.SENT -> RequestStatusEnumDto.SENT
                RequestStatusEnum.ACCEPTED -> RequestStatusEnumDto.ACCEPTED
                RequestStatusEnum.REJECTED -> RequestStatusEnumDto.REJECTED
                RequestStatusEnum.HIDDEN -> RequestStatusEnumDto.HIDDEN
                RequestStatusEnum.CANCELLED -> RequestStatusEnumDto.CANCELLED
            }
    }
}