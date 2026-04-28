package com.r8n.backend.migration.controller

import com.r8n.backend.migration.api.MigrationApi
import com.r8n.backend.migration.api.dto.ExportStateDto
import com.r8n.backend.migration.api.dto.UserCompleteDataDto
import com.r8n.backend.migration.facade.MigrationFacade
import com.r8n.backend.security.Authority
import com.r8n.backend.security.CurrentUserIdentifier.getCurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class MigrationController(
    private val migrationFacade: MigrationFacade,
) : MigrationApi {
    @PreAuthorize(Authority.IS_USER)
    override fun startGeneratingExportFor(): ResponseEntity<Void> {
        val userId = getCurrentUserId()
        migrationFacade.startExport(userId)
        return ResponseEntity.accepted().build()
    }

    @PreAuthorize(Authority.IS_USER)
    override fun checkIfDataIsReady(): ExportStateDto {
        val userId = getCurrentUserId()
        return migrationFacade.getExportStatus(userId)
    }

    @PreAuthorize(Authority.IS_USER)
    override fun downloadData(): UserCompleteDataDto {
        val userId = getCurrentUserId()
        return migrationFacade.getExportData(userId)
    }

    @PreAuthorize(Authority.IS_USER)
    override fun importData(file: MultipartFile): ResponseEntity<Void> {
        val userId = getCurrentUserId()
        migrationFacade.importData(userId, file)
        return ResponseEntity.ok().build()
    }
}
