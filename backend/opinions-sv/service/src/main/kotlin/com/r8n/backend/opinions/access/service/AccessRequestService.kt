package com.r8n.backend.opinions.access.service

import com.r8n.backend.opinions.access.database.persistence.AccessRequestRepository
import com.r8n.backend.opinions.access.domain.AccessRequest
import com.r8n.backend.opinions.access.domain.RequestStatusEnum
import com.r8n.backend.opinions.access.persistence.AccessRequestPersistence
import com.r8n.backend.opinions.lists.service.OpinionListService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AccessRequestService(
    private val repository: AccessRequestRepository,
    private val opinionListService: OpinionListService,
    private val usersClient: UsersInternalApi,
) {
    fun getRequests(
        listId: UUID?,
        requesterId: UUID?,
        ownerId: UUID?,
        status: RequestStatusEnum?,
        pageable: Pageable,
    ): Page<AccessRequest> =
        repository.findAllByFilters(listId, requesterId, ownerId, status, pageable).map { it.toDomain() }

    private companion object {
        fun AccessRequestPersistence.toDomain(): AccessRequest =
            AccessRequest(
                id = id,
                listId = listId,
                requesterId = requesterId,
                ownerId = ownerId,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
    }

    @Transactional
    fun createRequest(
        listId: UUID,
        requesterId: UUID,
    ): AccessRequest {
        val list = opinionListService.getList(listId)

        if (list.owner == requesterId) {
            throw IllegalArgumentException("Owner cannot request access to their own list")
        }

        val existingRequests =
            repository.findByRequesterIdAndListIdAndStatusIn(
                requesterId,
                listId,
                listOf(RequestStatusEnum.SENT, RequestStatusEnum.ACCEPTED, RequestStatusEnum.HIDDEN),
            )

        if (existingRequests.isNotEmpty()) {
            val existing = existingRequests.first()
            // If the owner has hidden the request, the requester shouldn't know.
            return existing.toDomain().apply { if (status == RequestStatusEnum.HIDDEN) status = RequestStatusEnum.SENT }
        }

        val now = Instant.now()
        val request =
            AccessRequestPersistence(
                listId = listId,
                requesterId = requesterId,
                ownerId = list.owner,
                status = RequestStatusEnum.SENT,
                createdAt = now,
                updatedAt = now,
            )
        return repository.save(request).toDomain()
    }

    @Transactional
    fun cancelRequest(
        requestId: UUID,
        requesterId: UUID,
    ): AccessRequest {
        val request =
            repository
                .findById(requestId)
                .orElseThrow { NoSuchElementException("Request not found") }

        if (request.requesterId != requesterId) {
            throw IllegalAccessException("Cannot cancel someone else's request")
        }

        request.status = RequestStatusEnum.CANCELLED
        request.updatedAt = Instant.now()
        return repository.save(request).toDomain()
    }

    @Transactional
    fun acceptRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequest {
        val request =
            repository
                .findById(requestId)
                .orElseThrow { NoSuchElementException("Request not found") }

        if (request.status == RequestStatusEnum.CANCELLED) {
            throw IllegalStateException("Cannot accept a cancelled request")
        }

        if (request.ownerId != ownerId) {
            throw IllegalAccessException("Only the list owner can accept requests")
        }

        request.status = RequestStatusEnum.ACCEPTED
        request.updatedAt = Instant.now()
        return repository.save(request).toDomain()
    }

    @Transactional
    fun declineRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequest {
        val request =
            repository
                .findById(requestId)
                .orElseThrow { NoSuchElementException("Request not found") }

        if (request.ownerId != ownerId) {
            throw IllegalAccessException("Only the list owner can decline requests")
        }

        request.status = RequestStatusEnum.REJECTED
        request.updatedAt = Instant.now()
        return repository.save(request).toDomain()
    }

    @Transactional
    fun hideRequest(
        requestId: UUID,
        ownerId: UUID,
    ): AccessRequest {
        val request =
            repository
                .findById(requestId)
                .orElseThrow { NoSuchElementException("Request not found") }

        if (request.ownerId != ownerId) {
            throw IllegalAccessException("Only the list owner can hide requests")
        }

        request.status = RequestStatusEnum.HIDDEN
        request.updatedAt = Instant.now()
        return repository.save(request).toDomain()
    }
}