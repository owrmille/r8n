package com.r8n.backend.opinions.lists.service

import com.r8n.backend.opinions.access.controller.service.AccessService
import com.r8n.backend.opinions.access.domain.OpinionListPermissionEnum
import com.r8n.backend.opinions.lists.database.OpinionListRepository
import com.r8n.backend.opinions.lists.database.OpinionsToOpinionListsRepository
import com.r8n.backend.opinions.lists.domain.OpinionList
import com.r8n.backend.opinions.lists.domain.OpinionSummary
import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
import com.r8n.backend.opinions.opinions.domain.WeightedOpinionReference
import com.r8n.backend.opinions.opinions.service.OpinionService
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class OpinionListService(
    private val opinionListRepository: OpinionListRepository,
    private val opinionService: OpinionService,
    private val opinionsAssignmentRepository: OpinionsToOpinionListsRepository,
    private val accessService: AccessService,
) {
    fun getList(listId: UUID): OpinionList {
        val list = opinionListRepository.findById(listId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val userId = getCurrentUserId()
        if (!accessService.canAccessOpinionList(userId, listId, OpinionListPermissionEnum.VIEW)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        val assignments = opinionsAssignmentRepository.findAllByOpinionListId(listId)
        val opinions = assignments.map { asmt ->
            opinionService.getOpinion(asmt.opinionId).copy(weight = asmt.weight)
        }
        val opinionsBySubject = opinions.groupBy { it.subject }

        val summaries = if (accessService.ownsOpinionList(userId, listId)) {
            opinionsBySubject.map { (subject, ops) ->
                val own = ops.firstOrNull { it.owner == userId }
                val weightedMarks = ops.mapNotNull { it.mark?.let { mark -> it.weight!! * mark } }
                val componentMark = weightedMarks.takeIf { it.isNotEmpty() && it.size == ops.size }?.sum()

                OpinionSummary(
                    subject = subject,
                    ownMark = own?.mark,
                    componentMark = componentMark,
                    opinions = ops.map { WeightedOpinionReference(id = it.id, opinion = it.id, weight = it.weight!!) }
                )
            }
        } else {
            // for another user I cannot see what his components are, I can see only his own opinion
            opinionsBySubject.mapNotNull { (subject, ops) ->
                val own = ops.firstOrNull { it.owner == userId }
                if (own == null) {
                    null
                } else {
                    OpinionSummary(
                        subject = subject,
                        ownMark = own.mark,
                        componentMark = null,
                        opinions = listOf(WeightedOpinionReference(id = own.id, opinion = own.id, weight = own.weight!!))
                    )
                }
            }
        }

        return toDomain(list, summaries)
    }
}

private fun toDomain(list: OpinionListPersistence, summaries: List<OpinionSummary>) = with(list) {
    OpinionList(
        id = id!!,
        name = name,
        owner = owner,
        opinionSummaries = summaries,
        privacy = privacy,
    )
}