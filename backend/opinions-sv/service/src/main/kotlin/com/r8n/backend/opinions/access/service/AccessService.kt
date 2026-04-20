package com.r8n.backend.opinions.access.controller.service

import com.r8n.backend.opinions.access.domain.AccessRequest
import com.r8n.backend.opinions.access.domain.OpinionListPermissionEnum
import com.r8n.backend.opinions.access.domain.OpinionPermissionEnum
import com.r8n.backend.opinions.access.domain.RequestStatusEnum
import com.r8n.backend.opinions.access.persistence.AccessRequestPersistence
import com.r8n.backend.opinions.access.database.persistence.AccessRequestRepository
import com.r8n.backend.opinions.integration.client.OpinionsInternalRestClient
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AccessService(
    private val repository: AccessRequestRepository,
    private val usersClient: UsersInternalApi,
) {
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

    fun ownsOpinion(userId: UUID, opinionId: UUID): Boolean {
        return false
    }

    fun ownsOpinionList(userId: UUID, opinionListId: UUID): Boolean {
        return false
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