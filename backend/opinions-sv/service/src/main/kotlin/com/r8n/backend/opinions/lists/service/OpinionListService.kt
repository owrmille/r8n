package com.r8n.backend.opinions.lists.service

import com.r8n.backend.opinions.access.controller.service.AccessService
import com.r8n.backend.opinions.access.domain.OpinionListPermissionEnum
import com.r8n.backend.opinions.lists.database.OpinionListRepository
import com.r8n.backend.opinions.lists.database.OpinionsToOpinionListsRepository
import com.r8n.backend.opinions.lists.domain.OpinionList
import com.r8n.backend.opinions.lists.domain.OpinionSummary
import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
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
        if (accessService.canAccessOpinionList(getCurrentUserId(), listId, OpinionListPermissionEnum.VIEW)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        val userId = getCurrentUserId()
        val opinions = opinionsAssignmentRepository.findAllByOpinionListId(listId)
            .map { opinionService.getOpinion(it.opinionId) }.groupBy { it.subject }
        var summaries: List<OpinionSummary>
        if (!accessService.ownsOpinionList(userId, listId)) {
            // for another user I cannot see what his components are, I can see only his own opinion
            summaries = opinions.map { (subject, ops) ->
                val own = ops.firstOrNull { it.owner == userId }
                OpinionSummary(
                    subject = subject,
                    ownMark = own?.mark,
                    componentMark = null,
                    opinions = listOf(own),
                )
            }
        }
    }

    private fun toDomain(list: OpinionListPersistence, summaries: List<OpinionSummary>) = with(list) {
        OpinionList(
            id = id!!,
            name = name,
            owner = owner,
            opinionSummaries = summaries
        )
    }
}