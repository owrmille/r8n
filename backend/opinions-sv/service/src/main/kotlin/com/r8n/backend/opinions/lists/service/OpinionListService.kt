package com.r8n.backend.opinions.lists.service

import com.r8n.backend.opinions.access.domain.OpinionListPermissionEnum
import com.r8n.backend.opinions.access.service.AccessService
import com.r8n.backend.opinions.lists.database.OpinionListRepository
import com.r8n.backend.opinions.lists.database.OpinionListSyncRepository
import com.r8n.backend.opinions.lists.database.OpinionsToOpinionListsRepository
import com.r8n.backend.opinions.lists.domain.OpinionList
import com.r8n.backend.opinions.lists.domain.OpinionListPrivacyEnum
import com.r8n.backend.opinions.lists.domain.OpinionSummary
import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
import com.r8n.backend.opinions.lists.persistence.OpinionListSyncPersistence
import com.r8n.backend.opinions.lists.persistence.OpinionsToOpinionListsPersistence
import com.r8n.backend.opinions.opinions.domain.WeightedOpinionReference
import com.r8n.backend.opinions.opinions.service.OpinionService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class OpinionListService(
    private val opinionListRepository: OpinionListRepository,
    private val opinionService: OpinionService,
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
        return getList(existingListId, userId)
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
        return getList(existingListId, userId)
    }

    fun getListName(
        listId: UUID,
        userId: UUID,
    ): String {
        val list =
            opinionListRepository
                .findById(listId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.ownsOpinionList(userId, listId) &&
            !accessService.canAccessOpinionList(userId, listId, OpinionListPermissionEnum.VIEW) &&
            list.privacy == OpinionListPrivacyEnum.PRIVATE
        ) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
        return list.name
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
                )
            )
        }
        return getList(listId, userId)
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
        return getList(listId, userId)
    }

    fun getList(
        listId: UUID,
        requesterId: UUID,
    ): OpinionList {
        val list =
            opinionListRepository
                .findById(listId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (!accessService.canAccessOpinionList(requesterId, listId, OpinionListPermissionEnum.VIEW)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        if (!accessService.ownsOpinionList(requesterId, listId) && list.privacy == OpinionListPrivacyEnum.PRIVATE) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        val assignments = opinionsAssignmentRepository.findAllByOpinionList(listId)
        val syncs = syncRepository.findAllByDestinationList(listId)

        val opinionWeightsInDestList = assignments.associate { it.opinion to it.weight }

        val syncedAssignments =
            syncs.flatMap { sync ->
                opinionsAssignmentRepository.findAllByOpinionList(sync.sourceList).map { asmt ->
                    val requesterWeight = opinionWeightsInDestList[asmt.opinion] ?: 1.0
                    asmt.copy(weight = requesterWeight * sync.weight)
                }
            }

        val allAssignments = assignments + syncedAssignments
        val opinions =
            allAssignments.mapNotNull { asmt ->
                try {
                    opinionService.getOpinion(asmt.opinion, requesterId).copy(weight = asmt.weight)
                } catch (e: ResponseStatusException) {
                    if (e.statusCode == HttpStatus.FORBIDDEN || e.statusCode == HttpStatus.NOT_FOUND) {
                        null
                    } else {
                        throw e
                    }
                }
            }
        val opinionsBySubject = opinions.groupBy { it.subject }

        val summaries =
            if (accessService.ownsOpinionList(requesterId, listId)) {
                opinionsBySubject.map { (subject, ops) ->
                    val own = ops.firstOrNull { it.owner == requesterId }
                    val weightedMarks = ops.mapNotNull { it.mark?.let { mark -> it.weight!! * mark } }
                    val totalWeight = ops.mapNotNull { if (it.mark != null) it.weight else null }.sum()
                    val componentMark = if (totalWeight > 0 && weightedMarks.size == ops.size) {
                        weightedMarks.sum() / totalWeight
                    } else {
                        null
                    }

                    OpinionSummary(
                        subject = subject,
                        ownMark = own?.mark,
                        componentMark = componentMark,
                        opinions =
                            ops.map {
                                WeightedOpinionReference(
                                    id = it.id,
                                    opinion = it.id,
                                    weight = it.weight!!,
                                )
                            },
                    )
                }
            } else {
                // for another user I cannot see what his components are, I can see only his own opinion
                opinionsBySubject.mapNotNull { (subject, ops) ->
                    val own = ops.firstOrNull { it.owner == list.owner }
                    if (own == null) {
                        null
                    } else {
                        OpinionSummary(
                            subject = subject,
                            ownMark = own.mark,
                            componentMark = null,
                            opinions =
                                listOf(
                                    WeightedOpinionReference(id = own.id, opinion = own.id, weight = own.weight!!),
                                ),
                        )
                    }
                }
            }

        return toDomain(list, summaries)
    }

    fun getListsFull(
        ownerId: UUID,
        pageable: Pageable,
    ): Page<OpinionList> =
        opinionListRepository
            .findByOwner(ownerId, pageable)
            .map { getList(it.id!!, ownerId) }

    private companion object {
        fun toDomain(
            list: OpinionListPersistence,
            summaries: List<OpinionSummary>,
        ) = with(list) {
            OpinionList(
                id = id!!,
                name = name,
                owner = owner,
                opinionSummaries = summaries,
                privacy = privacy,
            )
        }
    }
}
