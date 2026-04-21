package com.r8n.backend.opinions.access.facade

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.access.service.AccessRequestService
import com.r8n.backend.opinions.api.access.dto.AccessRequestDto
import com.r8n.backend.opinions.api.access.dto.RequestStatusEnumDto
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class AccessRequestFacade(
    private val service: AccessRequestService,
    private val accessRequestMapper: AccessRequestMapper,
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
        val page =
            service.getRequests(
                forListId,
                null,
                ownerId,
                accessRequestMapper.toDomain(status),
                pageRequest,
            )
        return PageResponseDto(
            items = page.content.map { accessRequestMapper.toDto(it) },
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
        val page =
            service.getRequests(
                forListId,
                requesterId,
                null,
                accessRequestMapper.toDomain(status),
                pageRequest,
            )
        return PageResponseDto(
            items = page.content.map { accessRequestMapper.toDto(it) },
            total = page.totalElements,
            page = pageable.page,
            size = pageable.size,
        )
    }

    fun createRequest(
        listId: UUID,
        requesterId: UUID,
    ): AccessRequestDto = accessRequestMapper.toDto(service.createRequest(listId, requesterId))

    fun cancelRequest(
        requestId: UUID,
        requesterId: UUID,
    ): AccessRequestDto = accessRequestMapper.toDto(service.cancelRequest(requestId, requesterId))

    fun acceptRequest(requestId: UUID): AccessRequestDto = accessRequestMapper.toDto(service.acceptRequest(requestId))

    fun declineRequest(requestId: UUID): AccessRequestDto = accessRequestMapper.toDto(service.declineRequest(requestId))

    fun hideRequest(requestId: UUID): AccessRequestDto = accessRequestMapper.toDto(service.hideRequest(requestId))
}