package com.r8n.backend.export.facade

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.export.api.dto.ExportStateDto
import com.r8n.backend.export.api.dto.ExportStatus
import com.r8n.backend.export.api.dto.UserCompleteDataDto
import com.r8n.backend.mock.api.IncomingAccessRequestApi
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.OutgoingAccessRequestApi
import com.r8n.backend.mock.integration.api.OpinionListInternalApi
import com.r8n.backend.users.api.dto.PersonalIdentifiableInformationSectionDto
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class DataExportFacade(
    private val usersClient: UsersInternalApi,
    private val opinionClient: OpinionListInternalApi,
    private val incomingAccessRequestClient: IncomingAccessRequestApi,
    private val outgoingAccessRequestClient: OutgoingAccessRequestApi,
    private val messageClient: MessagingApi,
) {
    private val logger = LoggerFactory.getLogger(DataExportFacade::class.java)
    private val exportJobs = ConcurrentHashMap<UUID, ExportJob>()

    data class ExportJob(
        val userId: UUID,
        val status: ExportJobStatus,
        val createdAt: Instant,
        val completedAt: Instant? = null,
        val result: UserCompleteDataDto? = null,
    )

    enum class ExportJobStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED,
    }

    fun startExport(userId: UUID) {
        logger.debug("Starting export for user: $userId")
        cancelExistingJob(userId)
        exportJobs[userId] = ExportJob(userId, ExportJobStatus.PENDING, Instant.now())

        // For now, process synchronously to ensure security context is properly propagated
        // In a real implementation, this would be an async job
        try {
            updateExportStatus(userId, ExportJobStatus.IN_PROGRESS)
            logger.debug("Fetching user data for user: $userId")
            val data = getUserCompleteDataDto(userId)
            logger.debug("User data fetched successfully for user: $userId")
            completeExport(userId, data)
            logger.debug("Export completed successfully for user: $userId")
        } catch (e: Exception) {
            logger.error("Export failed for user: $userId", e)
            failExport(userId)
        }
    }

    fun getExportStatus(userId: UUID): ExportStateDto {
        val job =
            exportJobs[userId]
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No export job found for user $userId")

        return ExportStateDto(
            userId = job.userId,
            status = job.status.toApiStatus(),
            createdAt = job.createdAt,
            completedAt = job.completedAt,
            estimatedCompletionTime = null,
        )
    }

    fun getExportData(userId: UUID): UserCompleteDataDto {
        val job =
            exportJobs[userId]
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No export job found for user $userId")

        if (job.status != ExportJobStatus.COMPLETED) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Export not completed yet. Status: ${job.status}",
            )
        }

        return job.result
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Completed export has no data for user $userId")
    }

    fun getUserCompleteDataDto(id: UUID): UserCompleteDataDto {
        val user = usersClient.getUser(id)
        val sessions = usersClient.getSessionsForUser(id, PageRequestDto(0, Int.MAX_VALUE))

        return UserCompleteDataDto(
            id = user.id,
            status = user.status,
            statusTimestamp = user.statusTimestamp,
            consents =
                PageResponseDto(
                    items = user.consents,
                    total = user.consents.size.toLong(),
                    page = 0,
                    size = user.consents.size,
                ),
            personalIdentifiableInformation =
                PersonalIdentifiableInformationSectionDto(
                    name = user.name,
                    email = user.email,
                    sessions = sessions,
                ),
            opinions = opinionClient.getMineFull(PageRequestDto(0, Int.MAX_VALUE)),
            outgoingRequests = outgoingAccessRequestClient.get(null, null, null, PageRequestDto(0, Int.MAX_VALUE)),
            incomingRequests = incomingAccessRequestClient.get(null, null, null, PageRequestDto(0, Int.MAX_VALUE)),
            messages = messageClient.getSupportThreads(),
        )
    }

    private fun ExportJobStatus.toApiStatus(): ExportStatus =
        when (this) {
            ExportJobStatus.PENDING -> ExportStatus.PENDING
            ExportJobStatus.IN_PROGRESS -> ExportStatus.IN_PROGRESS
            ExportJobStatus.COMPLETED -> ExportStatus.COMPLETED
            ExportJobStatus.FAILED -> ExportStatus.FAILED
            ExportJobStatus.CANCELLED -> ExportStatus.FAILED
        }

    private fun cancelExistingJob(userId: UUID) {
        exportJobs[userId]?.let {
            exportJobs[userId] = it.copy(status = ExportJobStatus.CANCELLED)
        }
    }

    private fun updateExportStatus(
        userId: UUID,
        status: ExportJobStatus,
    ) {
        exportJobs[userId]?.let { job ->
            exportJobs[userId] = job.copy(status = status)
        }
    }

    private fun completeExport(
        userId: UUID,
        data: UserCompleteDataDto,
    ) {
        exportJobs[userId]?.let { job ->
            exportJobs[userId] =
                job.copy(
                    status = ExportJobStatus.COMPLETED,
                    completedAt = Instant.now(),
                    result = data,
                )
        }
    }

    private fun failExport(userId: UUID) {
        exportJobs[userId]?.let { job ->
            exportJobs[userId] =
                job.copy(
                    status = ExportJobStatus.FAILED,
                    completedAt = Instant.now(),
                )
        }
    }
}