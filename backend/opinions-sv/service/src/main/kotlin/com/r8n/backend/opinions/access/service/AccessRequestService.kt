package com.r8n.backend.opinions.access.service

import com.r8n.backend.opinions.access.database.AccessRequestRepository
import com.r8n.backend.opinions.access.domain.AccessRequest
import com.r8n.backend.opinions.access.domain.RequestStatusEnum
import com.r8n.backend.opinions.access.persistence.AccessRequestPersistence
import com.r8n.backend.opinions.lists.service.OpinionListService
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
) {
    fun getRequests(
        listId: UUID?,
        requesterId: UUID?,
        ownerId: UUID?,
        status: RequestStatusEnum?,
        pageable: Pageable,
    ): Page<AccessRequest> =
        repository.findAllByFilters(listId, requesterId, ownerId, status, pageable).map {
            it.toDomain()
        }

    private fun AccessRequestPersistence.toDomain(): AccessRequest =
        AccessRequest(
            id = id,
            listId = list,
            requesterId = requester,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            ownerId = opinionListService.getListOwner(list),
        )

    @Transactional
    fun createRequest(
        listId: UUID,
        requesterId: UUID,
    ): AccessRequest {
        val list = opinionListService.getList(listId, requesterId)

        if (list.owner == requesterId) {
            throw IllegalArgumentException("Owner cannot request access to their own list")
        }

        val existingRequests =
            repository.findByRequesterAndListAndStatusIn(
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
                list = listId,
                requester = requesterId,
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

        if (request.requester != requesterId) {
            throw IllegalAccessException("Cannot cancel someone else's request")
        }

        if (request.status == RequestStatusEnum.CANCELLED) {
            throw IllegalStateException("Request already cancelled")
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

        if (opinionListService.getListOwner(request.list) != ownerId) {
            throw SecurityException("Only the owner can accept a request")
        }

        if (request.status != RequestStatusEnum.SENT) {
            throw IllegalStateException("Can only accept a pending request")
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

        if (opinionListService.getListOwner(request.list) != ownerId) {
            throw SecurityException("Only the owner can decline a request")
        }

        if (request.status == RequestStatusEnum.REJECTED) {
            throw IllegalStateException("Request already rejected")
        }

        if (request.status == RequestStatusEnum.CANCELLED) {
            throw IllegalStateException("Cannot decline a cancelled request")
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

        if (opinionListService.getListOwner(request.list) != ownerId) {
            throw SecurityException("Only the owner can hide a request")
        }

        if (request.status != RequestStatusEnum.SENT) {
            throw IllegalStateException("Can only hide a pending request")
        }

        request.status = RequestStatusEnum.HIDDEN
        request.updatedAt = Instant.now()
        return repository.save(request).toDomain()
    }
}