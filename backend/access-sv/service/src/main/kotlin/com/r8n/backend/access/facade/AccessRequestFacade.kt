package com.r8n.backend.access.facade

import com.r8n.backend.access.api.dto.access.AccessRequestDto
import com.r8n.backend.access.api.dto.access.RequestStatusEnumDto
import com.r8n.backend.access.domain.PermissionEnum
import com.r8n.backend.access.domain.RequestStatusEnum
import com.r8n.backend.access.integration.api.PermissionEnumDto
import com.r8n.backend.access.persistence.AccessRequestPersistence
import com.r8n.backend.access.service.AccessRequestService
import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.mock.api.OpinionListApi
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
    private val opinionListApi: OpinionListApi,
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
            items = page.content.map { toDto(it) },
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
            items = page.content.map { toDto(it) },
            total = page.totalElements,
            page = pageable.page,
            size = pageable.size,
        )
    }

    fun createRequest(
        listId: UUID,
        requesterId: UUID,
    ): AccessRequestDto = toDto(service.createRequest(listId, requesterId))

    fun cancelRequest(
        requestId: UUID,
        requesterId: UUID,
    ): AccessRequestDto = toDto(service.cancelRequest(requestId, requesterId))

    fun acceptRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequestDto = toDto(service.acceptRequest(requestId, ownerId))

    fun declineRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequestDto = toDto(service.declineRequest(requestId, ownerId))

    fun hideRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequestDto = toDto(service.hideRequest(requestId, ownerId))

    private fun toDto(persistence: AccessRequestPersistence): AccessRequestDto {
        val requesterName =
            try {
                usersInternalApi.getUserName(persistence.requesterId)
            } catch (_: Exception) {
                "Unknown"
            }
        val ownerName =
            try {
                usersInternalApi.getUserName(persistence.ownerId)
            } catch (_: Exception) {
                "Unknown"
            }
        val listName =
            try {
                opinionListApi.getListSummary(persistence.listId).listName
            } catch (_: Exception) {
                "Unknown"
            }

        return AccessRequestDto(
            id = persistence.id!!,
            opinionListId = persistence.listId,
            opinionListName = listName,
            owner = persistence.ownerId,
            ownerName = ownerName,
            requester = persistence.requesterId,
            requesterName = requesterName,
            timestamp = persistence.createdAt,
            status = persistence.status.toDto(),
        )
    }

    fun canAccessOpinion(
        userId: UUID,
        opinionId: UUID,
        permission: PermissionEnumDto,
    ): Boolean = service.canAccessOpinion(userId, opinionId, permission.toDomain())

    fun canAccessOpinionList(
        userId: UUID,
        opinionListId: UUID,
        permission: PermissionEnumDto,
    ): Boolean = service.canAccessOpinionList(userId, opinionListId, permission.toDomain())

    private companion object {
        fun PermissionEnumDto.toDomain() =
            when (this) {
                PermissionEnumDto.READ -> PermissionEnum.READ
                PermissionEnumDto.EDIT -> PermissionEnum.EDIT
                PermissionEnumDto.DELETE -> PermissionEnum.DELETE
                PermissionEnumDto.APPROVE -> PermissionEnum.APPROVE
                PermissionEnumDto.REJECT -> PermissionEnum.REJECT
            }

        fun PermissionEnum.toDto() =
            when (this) {
                PermissionEnum.READ -> PermissionEnumDto.READ
                PermissionEnum.EDIT -> PermissionEnumDto.EDIT
                PermissionEnum.DELETE -> PermissionEnumDto.DELETE
                PermissionEnum.APPROVE -> PermissionEnumDto.APPROVE
                PermissionEnum.REJECT -> PermissionEnumDto.REJECT
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