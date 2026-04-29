package com.r8n.backend.migration.api

import com.r8n.backend.migration.api.dto.ExportStateDto
import com.r8n.backend.migration.api.dto.UserCompleteDataDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Data migration", description = "Self-service export and import of the authenticated user's stored data.")
interface Migration {
    companion object {
        private const val BASE_PATH = "/api/export"
        const val START_PATH = "$BASE_PATH/start"
        const val STATUS_PATH = "$BASE_PATH/status"
        const val DOWNLOAD_PATH = "$BASE_PATH/download"
        const val IMPORT_PATH = "$BASE_PATH/import"
    }

    @PostMapping(START_PATH)
    @Operation(
        summary = "Start data export",
        description = "Queues generation of the authenticated user's complete data export and returns immediately.",
    )
    fun startGeneratingExportFor(): ResponseEntity<Void>

    @GetMapping(STATUS_PATH)
    @Operation(
        summary = "Get export status",
        description = "Returns whether the authenticated user's generated data export is ready to download.",
    )
    fun checkIfDataIsReady(): ExportStateDto

    @GetMapping(DOWNLOAD_PATH)
    @Operation(
        summary = "Download data export",
        description = "Returns the authenticated user's generated data export when it is ready.",
    )
    fun downloadData(): UserCompleteDataDto

    @PostMapping(IMPORT_PATH, consumes = ["multipart/form-data"])
    @Operation(
        summary = "Data import",
        description = "Imports a JSON, expecting it to be R8N's complete export for current user. Restores opinion and lists, but not syncs - only resends access requests.",
    )
    fun importData(
        @RequestPart("file")
        file: MultipartFile,
    ): ResponseEntity<Void>
}
