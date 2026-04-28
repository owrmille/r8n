package com.r8n.backend.migration.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.r8n.backend.migration.api.dto.UserCompleteDataDto
import com.r8n.backend.opinions.api.access.OutgoingAccessRequestApi
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.opinions.integration.api.OpinionsInternalApi
import com.r8n.backend.users.integration.api.UsersInternalApi
import com.r8n.backend.users.integration.api.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class DataImportService(
    private val usersClient: UsersInternalApi,
    private val opinionListsClient: OpinionListsInternalApi,
    private val opinionsClient: OpinionsInternalApi,
    private val outgoingAccessRequestClient: OutgoingAccessRequestApi,
    private val objectMapper: ObjectMapper,
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
                id = data.id,
                name = data.personalIdentifiableInformation.name,
                email = data.personalIdentifiableInformation.email,
                status = data.status,
                statusTimestamp = data.statusTimestamp,
                consents = data.consents.items,
            )
        usersClient.restoreUser(userDto)
        logger.debug("User restored: {}", data.id)

        data.myFullOpinions.forEach { opinion ->
            opinionsClient.restoreOpinion(opinion)
        }
        logger.debug("Opinions restored for user: {}", data.id)

        data.opinions.items.forEach { list ->
            opinionListsClient.restoreOpinionList(list)
        }
        logger.debug("Opinion lists restored for user: {}", data.id)

        // Re-create Outgoing Access Requests as PENDING
        data.outgoingRequests.items.forEach { request ->
            try {
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
