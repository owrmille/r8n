package com.r8n.backend.migration.service

import com.r8n.backend.migration.api.dto.UserCompleteDataDto
import com.r8n.backend.opinions.api.access.OutgoingAccessRequestApi
import com.r8n.backend.opinions.api.access.dto.AccessRequestIntentDto
import com.r8n.backend.opinions.api.lists.OpinionListsApi
import com.r8n.backend.opinions.api.opinions.OpinionsApi
import com.r8n.backend.users.api.dto.UserStatusEnumDto
import com.r8n.backend.users.integration.api.UsersInternalApi
import com.r8n.backend.users.integration.api.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@Service
class DataImportService(
    private val usersClient: UsersInternalApi,
    private val opinionsClient: OpinionsApi,
    private val opinionListsClient: OpinionListsApi,
    private val outgoingAccessRequestClient: OutgoingAccessRequestApi,
    private val objectMapper: JsonMapper,
) {
    private val logger = LoggerFactory.getLogger(DataImportService::class.java)

    private fun personal(
        userId: UUID,
        data: UserCompleteDataDto,
    ) {
        val userDto =
            UserDto(
                name = data.personalIdentifiableInformation.name,
                email = data.personalIdentifiableInformation.email,
                status = UserStatusEnumDto.ACTIVE,
                statusTimestamp = Instant.now(),
                consents = data.consents.items,
            )
        try {
            usersClient.restoreUser(userDto)
        } catch (e: Exception) {
            logger.error("Error restoring user: {}", e.message)
            throw e
        }
        logger.debug("User restored: {}", userId)
    }

    private fun opinions(
        userId: UUID,
        data: UserCompleteDataDto,
    ): Map<UUID, UUID> {
        /* list 0 is a virtual list of all user's opinions,
        so we populate opinions from it,
        and then, when the time comes for real opinion lists,
        we add these created opinions to those lists
         */
        val subjectToOpinionId = mutableMapOf<UUID, UUID>()
        val oldOpinionIdToNewId = mutableMapOf<UUID, UUID>()
        if (data.opinions.items.isEmpty()) {
            return subjectToOpinionId
        }
        data.opinions.items[0].opinionSummaries.forEach { opinionSummary ->
            with(opinionSummary) {
                val createdOpinion =
                    try {
                        opinionsClient.createOpinion(
                            subjectId = subject,
                            subjective = opinions[0].subjective,
                            objective = opinions[0].objective,
                            mark = ownMark,
                        )
                    } catch (e: Exception) {
                        logger.error("Error restoring opinion {}: {}", subject, e.message)
                        throw e
                    }
                subjectToOpinionId[subject] = createdOpinion.id
                oldOpinionIdToNewId[opinions[0].opinionId] = createdOpinion.id
            }
        }

        // Restore links (components) between OWN opinions
        data.opinions.items[0].opinionSummaries.forEach { summary ->
            summary.opinions.forEach { row ->
                val parentOpinionId = oldOpinionIdToNewId[row.opinionId]
                if (parentOpinionId != null) {
                    row.components.forEach { component ->
                        val childOpinionId = oldOpinionIdToNewId[component.opinion]
                        if (childOpinionId != null) {
                            try {
                                opinionsClient.linkComponent(
                                    parentOpinionId = parentOpinionId,
                                    childOpinionId = childOpinionId,
                                    weight = component.weight,
                                )
                            } catch (e: Exception) {
                                logger.error("Could not restore component link: {}", e.message)
                                throw e
                            }
                        }
                    }
                }
            }
        }

        logger.debug("Opinions restored for user: {}", userId)
        return subjectToOpinionId
    }

    private fun opinionLists(
        userId: UUID,
        subjectToOpinionId: Map<UUID, UUID>,
        data: UserCompleteDataDto,
    ) {
        // Create opinion lists (skipping the virtual one at index 0)
        data.opinions.items.drop(1).forEach { listDto ->
            val createdList =
                try {
                    opinionListsClient.createList(
                        name = listDto.listName,
                        privacy = listDto.privacy,
                    )
                } catch (e: Exception) {
                    logger.error("Error restoring opinion list {}: {}", listDto.listName, e.message)
                    throw e
                }

            // Link opinions to the newly created list
            listDto.opinionSummaries.forEach { summary ->
                val opinionId = subjectToOpinionId[summary.subject]
                if (opinionId != null) {
                    summary.opinions.forEach { opinionRow ->
                        try {
                            opinionListsClient.linkOpinion(
                                listId = createdList.id!!,
                                opinionId = opinionId,
                                weight = opinionRow.weight,
                            )
                        } catch (e: Exception) {
                            logger.error(
                                "Error linking opinion {} to list {}: {}",
                                opinionId,
                                createdList.id,
                                e.message,
                            )
                            throw e
                        }
                    }
                }
            }
        }
        logger.debug("Opinion lists restored for user: {}", userId)
    }

    private fun sendRequests(
        data: UserCompleteDataDto,
    ) {
        // Re-create Outgoing Access Requests as PENDING
        data.outgoingRequests.items.forEach { request ->
            try {
                outgoingAccessRequestClient.create(request.opinionListId, AccessRequestIntentDto.COPY, null)
            } catch (e: Exception) {
                logger.error(
                    "Could not re-create outgoing access request for list {}: {}",
                    request.opinionListId,
                    e.message,
                )
                throw e
            }
        }
    }

    fun importData(
        userId: UUID,
        file: MultipartFile,
    ) {
        logger.info("Starting data import for user: {}", userId)
        if (file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty")
        }
        val data =
            try {
                objectMapper.readValue(file.inputStream, UserCompleteDataDto::class.java)
            } catch (e: Exception) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file format", e)
            }
        try {
            personal(userId, data)
            val subjectToOpinionId = opinions(userId, data)
            opinionLists(userId, subjectToOpinionId, data)
            sendRequests(data)
        } catch (e: Exception) {
            logger.error("Data import failed for user {}: {}", userId, e.message)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Data import failed: ${e.message}", e)
        }
        logger.info("Data import completed for user: {}", userId)
    }
}
