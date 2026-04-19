package com.r8n.backend.access.service

import com.r8n.backend.access.domain.AccessRequest
import com.r8n.backend.access.domain.OpinionListPermissionEnum
import com.r8n.backend.access.domain.OpinionPermissionEnum
import com.r8n.backend.access.domain.RequestStatusEnum
import com.r8n.backend.access.persistence.AccessRequestPersistence
import com.r8n.backend.access.persistence.AccessRequestRepository
import com.r8n.backend.mock.api.OpinionListApi
import com.r8n.backend.opinions.integration.client.OpinionsInternalRestClient
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
    private val opinionsRestClient: OpinionsInternalRestClient,
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
        val list = opinionListApi.getList(listId)

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

    private fun roleBased(
        userId: UUID,
        ownerId: UUID,
        permission: OpinionPermissionEnum,
    ): Boolean? {
        if (usersClient.isAnyModerator(userId) &&
            (permission == OpinionPermissionEnum.READ || permission == OpinionPermissionEnum.REJECT)
        ) {
            return true
        }
        if (usersClient.isHumanModerator(userId) && permission == OpinionPermissionEnum.APPROVE) {
            return true
        }
        if (ownerId == userId &&
            (
                permission == OpinionPermissionEnum.READ ||
                    permission == OpinionPermissionEnum.EDIT ||
                    permission == OpinionPermissionEnum.DELETE
            )
        ) {
            return true
        }
        if (permission != OpinionPermissionEnum.READ) {
            return false
        }
        return null
    }

    fun canAccessOpinion(
        userId: UUID,
        opinionId: UUID,
        permission: OpinionPermissionEnum,
    ): Boolean {
        val ownerId = opinionsRestClient.getOwnerOfOpinion(opinionId)
        val roleBased = roleBased(userId, ownerId, permission)
        if (roleBased != null) return roleBased

        // READ-only: has an access request to any of the lists containing this opinion been approved?
        // we can also first fetch all owner's lists that contain this opinion and check if the requester has access to any of them
        // not sure what would be more efficient in the long run

        val activeRequests =
            repository.findAllByFilters(
                listId = null,
                requesterId = userId,
                ownerId = ownerId,
                status = RequestStatusEnum.ACCEPTED,
                pageable = Pageable.unpaged(),
            )

        return activeRequests.any { request ->
            val list = opinionListApi.getList(request.listId)
            list.opinionSummaries.any { it.id == opinionId }
        }
    }

    fun canAccessOpinionList(
        userId: UUID,
        listId: UUID,
        permission: OpinionListPermissionEnum,
    ): Boolean {
        if (permission != OpinionPermissionEnum.READ) return false

        val list =
            try {
                opinionListApi.getList(listId)
            } catch (_: Exception) {
                return false
            }
        if (list.owner == userId) return true

        return repository
            .findFirstByRequesterIdAndListIdAndStatus(
                userId,
                listId,
                RequestStatusEnum.ACCEPTED,
            ).isPresent
    }
}