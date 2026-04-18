package com.r8n.backend.access.service

import com.r8n.backend.access.domain.PermissionEnum
import com.r8n.backend.access.domain.RequestStatusEnum
import com.r8n.backend.access.persistence.AccessRequestPersistence
import com.r8n.backend.access.persistence.AccessRequestRepository
import com.r8n.backend.mock.api.OpinionListApi
import com.r8n.backend.opinions.integration.client.OpinionsInternalRestClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AccessRequestService(
    private val repository: AccessRequestRepository,
    private val opinionListApi: OpinionListApi,
    private val opinionsRestClient: OpinionsInternalRestClient,
) {

    fun getRequests(
        listId: UUID?,
        requesterId: UUID?,
        ownerId: UUID?,
        status: RequestStatusEnum?,
        pageable: Pageable
    ): Page<AccessRequestPersistence> {
        return repository.findAllByFilters(listId, requesterId, ownerId, status, pageable)
    }

    @Transactional
    fun createRequest(listId: UUID, requesterId: UUID): AccessRequestPersistence {
        val list = opinionListApi.getList(listId)
        
        if (list.owner == requesterId) {
            throw IllegalArgumentException("Owner cannot request access to their own list")
        }

        val existingRequests = repository.findByRequesterIdAndListIdAndStatusIn(
            requesterId, listId, listOf(RequestStatusEnum.SENT, RequestStatusEnum.ACCEPTED)
        )
        if (existingRequests.isNotEmpty()) {
            return existingRequests.first()
        }

        val now = Instant.now()
        val request = AccessRequestPersistence(
            listId = listId,
            requesterId = requesterId,
            ownerId = list.owner,
            status = RequestStatusEnum.SENT,
            createdAt = now,
            updatedAt = now
        )
        return repository.save(request)
    }

    @Transactional
    fun cancelRequest(requestId: UUID, requesterId: UUID): AccessRequestPersistence {
        val request = repository.findById(requestId)
            .orElseThrow { NoSuchElementException("Request not found") }
        
        if (request.requesterId != requesterId) {
            throw IllegalAccessException("Cannot cancel someone else's request")
        }
        
        if (request.status != RequestStatusEnum.SENT) {
            throw IllegalStateException("Can only cancel requests in SENT status")
        }

        request.status = RequestStatusEnum.CANCELLED
        request.updatedAt = Instant.now()
        return repository.save(request)
    }

    @Transactional
    fun acceptRequest(requestId: UUID, ownerId: UUID): AccessRequestPersistence {
        val request = repository.findById(requestId)
            .orElseThrow { NoSuchElementException("Request not found") }

        if (request.ownerId != ownerId) {
            throw IllegalAccessException("Only the list owner can accept requests")
        }

        if (request.status != RequestStatusEnum.SENT) {
            throw IllegalStateException("Can only accept requests in SENT status")
        }

        request.status = RequestStatusEnum.ACCEPTED
        request.updatedAt = Instant.now()
        return repository.save(request)
    }

    @Transactional
    fun declineRequest(requestId: UUID, ownerId: UUID): AccessRequestPersistence {
        val request = repository.findById(requestId)
            .orElseThrow { NoSuchElementException("Request not found") }

        if (request.ownerId != ownerId) {
            throw IllegalAccessException("Only the list owner can decline requests")
        }

        if (request.status != RequestStatusEnum.SENT) {
            throw IllegalStateException("Can only decline requests in SENT status")
        }

        request.status = RequestStatusEnum.REJECTED
        request.updatedAt = Instant.now()
        return repository.save(request)
    }

    @Transactional
    fun hideRequest(requestId: UUID, ownerId: UUID): AccessRequestPersistence {
        val request = repository.findById(requestId)
            .orElseThrow { NoSuchElementException("Request not found") }

        if (request.ownerId != ownerId) {
            throw IllegalAccessException("Only the list owner can hide requests")
        }

        request.status = RequestStatusEnum.HIDDEN
        request.updatedAt = Instant.now()
        return repository.save(request)
    }

    fun canAccessOpinion(userId: UUID, opinionId: UUID, permission: PermissionEnum): Boolean {
        val ownerId = opinionsRestClient.getOwnerOfOpinion(opinionId)
        if (permission == PermissionEnum.READ) {
            // we can also first fetch all owner's lists that contain this opinion and check if the requester has access to any of them
            // not sure what would be more efficient in the long run

            val activeRequests = repository.findAllByFilters(
                listId = null,
                requesterId = userId,
                ownerId = ownerId,
                status = RequestStatusEnum.ACCEPTED,
                pageable = Pageable.unpaged()
            )

            return activeRequests.any { request ->
                val list = opinionListApi.getList(request.listId)
                list.opinionSummaries.any { it.id == opinionId }
            }
        } else if (permission == PermissionEnum.EDIT || permission == PermissionEnum.DELETE) {
            return ownerId == userId
        } else if (permission == PermissionEnum.APPROVE || permission == PermissionEnum.REJECT) {
            return
        }
    }

    fun canAccessOpinionList(userId: UUID, listId: UUID, permission: PermissionEnum): Boolean {
        if (permission != PermissionEnum.READ) return false
        
        val list = try {
            opinionListApi.getList(listId)
        } catch (_: Exception) {
            return false
        }
        if (list.owner == userId) return true

        return repository.findFirstByRequesterIdAndListIdAndStatus(
            userId, listId, RequestStatusEnum.ACCEPTED
        ).isPresent
    }
}
