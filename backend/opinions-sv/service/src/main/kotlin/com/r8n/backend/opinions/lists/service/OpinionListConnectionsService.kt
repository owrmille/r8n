package com.r8n.backend.opinions.lists.service

import com.r8n.backend.opinions.access.domain.OpinionListPermissionEnum
import com.r8n.backend.opinions.access.service.AccessService
import com.r8n.backend.opinions.lists.database.OpinionListRepository
import com.r8n.backend.opinions.lists.database.OpinionListSyncRepository
import com.r8n.backend.opinions.lists.database.OpinionsToOpinionListsRepository
import com.r8n.backend.opinions.lists.domain.OpinionList
import com.r8n.backend.opinions.lists.domain.OpinionListPrivacyEnum
import com.r8n.backend.opinions.lists.persistence.OpinionListSyncPersistence
import com.r8n.backend.opinions.lists.persistence.OpinionsToOpinionListsPersistence
import com.r8n.backend.opinions.opinions.service.OpinionService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class OpinionListConnectionsService(
    private val opinionListRepository: OpinionListRepository,
    private val opinionService: OpinionService,
    private val opinionListService: OpinionListService,
    private val opinionsAssignmentRepository: OpinionsToOpinionListsRepository,
    private val accessService: AccessService,
    private val syncRepository: OpinionListSyncRepository,
) {
    fun syncWithOpinionList(
        userId: UUID,
        existingListId: UUID,
        addedListId: UUID,
        weight: Double = 1.0,
    ): OpinionList {
        if (!accessService.ownsOpinionList(userId, existingListId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own the destination list")
        }
        val addedList =
            opinionListRepository.findById(addedListId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND)
            }
        if (addedList.privacy == OpinionListPrivacyEnum.PRIVATE) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
        if (!accessService.canAccessOpinionList(userId, addedListId, OpinionListPermissionEnum.VIEW)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to the source list")
        }

        val existingSync = syncRepository.findByDestinationListAndSourceList(existingListId, addedListId)
        if (existingSync != null) {
            if (existingSync.weight != weight) {
                existingSync.weight = weight
                syncRepository.save(existingSync)
            }
        } else {
            syncRepository.save(
                OpinionListSyncPersistence(
                    destinationList = existingListId,
                    sourceList = addedListId,
                    weight = weight,
                ),
            )
        }
        return opinionListService.getList(existingListId, userId)
    }

    @Transactional
    fun unsyncWithOpinionList(
        userId: UUID,
        existingListId: UUID,
        removedListId: UUID,
    ): OpinionList {
        if (!accessService.ownsOpinionList(userId, existingListId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own the destination list")
        }

        syncRepository.deleteByDestinationListAndSourceList(existingListId, removedListId)
        return opinionListService.getList(existingListId, userId)
    }

    @Transactional
    fun linkOpinion(
        userId: UUID,
        listId: UUID,
        opinionId: UUID,
        weight: Double,
    ): OpinionList {
        if (!accessService.ownsOpinionList(userId, listId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own this list")
        }
        // check opinion access
        opinionService.getOpinion(opinionId, userId)

        val existing = opinionsAssignmentRepository.findAllByOpinionList(listId).find { it.opinion == opinionId }
        if (existing != null) {
            existing.weight = weight
            opinionsAssignmentRepository.save(existing)
        } else {
            opinionsAssignmentRepository.save(
                OpinionsToOpinionListsPersistence(
                    opinionList = listId,
                    opinion = opinionId,
                    weight = weight,
                ),
            )
        }
        return opinionListService.getList(listId, userId)
    }

    @Transactional
    fun unlinkOpinion(
        userId: UUID,
        listId: UUID,
        opinionId: UUID,
    ): OpinionList {
        if (!accessService.ownsOpinionList(userId, listId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own this list")
        }
        val assignments = opinionsAssignmentRepository.findAllByOpinionList(listId)
        val toRemove = assignments.filter { it.opinion == opinionId }
        opinionsAssignmentRepository.deleteAll(toRemove)
        return opinionListService.getList(listId, userId)
    }

    @Transactional
    fun moveOpinion(
        userId: UUID,
        fromListId: UUID,
        toListId: UUID,
        opinionId: UUID,
        weight: Double = 1.0,
    ): OpinionList {
        if (fromListId == toListId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "fromListId and toListId must differ")
        }
        val ret = linkOpinion(userId, toListId, opinionId, weight)
        unlinkOpinion(userId, fromListId, opinionId)
        return ret
    }
}
