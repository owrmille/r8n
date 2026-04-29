package com.r8n.backend.migration.service

import com.r8n.backend.migration.api.dto.UserCompleteDataDto
import com.r8n.backend.opinions.api.access.OutgoingAccessRequestApi
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.opinions.integration.api.OpinionsInternalApi
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
    private val opinionListsClient: OpinionListsInternalApi,
    private val opinionsClient: OpinionsInternalApi,
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

        data.myFullOpinions.forEach { opinion ->
            opinion.owner = userId
            opinionsClient.restoreOpinion(opinion)
        }
        logger.debug("Opinions restored for user: {}", userId)

        data.opinions.items.forEach { list ->
            list.owner = userId
            opinionListsClient.restoreOpinionList(list)
        }
        logger.debug("Opinion lists restored for user: {}", userId)

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
