package com.r8n.backend.migration.service

import com.r8n.backend.migration.api.dto.UserCompleteDataDto
import com.r8n.backend.opinions.api.access.OutgoingAccessRequestApi
import com.r8n.backend.opinions.api.opinions.OpinionsApi
import com.r8n.backend.users.api.dto.UserStatusEnumDto
import com.r8n.backend.users.integration.api.UsersInternalApi
import com.r8n.backend.users.integration.api.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@Service
class DataImportService(
    private val usersClient: UsersInternalApi,
    private val opinionsClient: OpinionsApi,
    private val outgoingAccessRequestClient: OutgoingAccessRequestApi,
    private val objectMapper: JsonMapper,
) {
    private val logger = LoggerFactory.getLogger(DataImportService::class.java)

    fun importData(
        userId: UUID,
        file: MultipartFile,
    ) {
        logger.info("Starting data import for user: {}", userId)
        val data = objectMapper.readValue(file.inputStream, UserCompleteDataDto::class.java)

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

        /* list 0 is a virtual list of all user's opinions,
        so we populate opinions from it,
        and then, when the time comes for real opinion lists,
        we add these created opinions to those lists
         */
        data.opinions.items[0].opinionSummaries.forEach { opinionSummary ->
            with(opinionSummary) {
                opinionsClient.createOpinion(
                    subjectId = subject,
                    subjective = opinions[0].subjective,
                    objective = opinions[0].objective,
                    mark = ownMark,
                )
            }
            logger.debug("Opinions restored for user: {}", userId)

            // TODO

            // Re-create Outgoing Access Requests as PENDING
            data.outgoingRequests.items.forEach { request ->
                try {
                    request.requester = userId
                    outgoingAccessRequestClient.create(request.opinionListId)
                } catch (e: Exception) {
                    logger.warn(
                        "Could not re-create outgoing access request for list {}: {}",
                        request.opinionListId,
                        e.message,
                    )
                }
            }
        }
    }
}
