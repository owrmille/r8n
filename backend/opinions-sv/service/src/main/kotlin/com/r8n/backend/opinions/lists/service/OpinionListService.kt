package com.r8n.backend.opinions.lists.service

import com.r8n.backend.opinions.access.domain.OpinionListPermissionEnum
import com.r8n.backend.opinions.access.service.AccessService
import com.r8n.backend.opinions.lists.database.OpinionListRepository
import com.r8n.backend.opinions.lists.database.OpinionsToOpinionListsRepository
import com.r8n.backend.opinions.lists.domain.OpinionList
import com.r8n.backend.opinions.lists.domain.OpinionListPrivacyEnum
import com.r8n.backend.opinions.lists.domain.OpinionSummary
import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
import com.r8n.backend.opinions.opinions.domain.WeightedOpinionReference
import com.r8n.backend.opinions.opinions.service.OpinionService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class OpinionListService(
    private val opinionListRepository: OpinionListRepository,
    private val opinionService: OpinionService,
    private val opinionsAssignmentRepository: OpinionsToOpinionListsRepository,
    private val accessService: AccessService,
) {
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
        val opinions =
            assignments.map { asmt ->
                opinionService.getOpinion(asmt.opinion, requesterId).copy(weight = asmt.weight)
            }
        val opinionsBySubject = opinions.groupBy { it.subject }

        val summaries =
            if (accessService.ownsOpinionList(requesterId, listId)) {
                opinionsBySubject.map { (subject, ops) ->
                    val own = ops.firstOrNull { it.owner == requesterId }
                    val weightedMarks = ops.mapNotNull { it.mark?.let { mark -> it.weight!! * mark } }
                    val componentMark = weightedMarks.takeIf { it.isNotEmpty() && it.size == ops.size }?.sum()

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

    fun deleteAllUserDataForUser(userId: UUID) {
        // Delete all opinion lists owned by the user
        // This will cascade to delete opinions-to-lists relationships
        val userLists = opinionListRepository.findAllByOwner(userId)
        opinionListRepository.deleteAll(userLists)
    }

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
