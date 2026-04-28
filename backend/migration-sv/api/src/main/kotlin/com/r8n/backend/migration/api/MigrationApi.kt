package com.r8n.backend.migration.api

import com.r8n.backend.migration.api.dto.ExportStateDto
import com.r8n.backend.migration.api.dto.UserCompleteDataDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

interface MigrationApi {
    companion object {
        private const val EXPORT_PATH = "/api/export"
        const val START_PATH = "$EXPORT_PATH/start"
        const val STATUS_PATH = "$EXPORT_PATH/status"
        const val DOWNLOAD_PATH = "$EXPORT_PATH/download"
        const val IMPORT_PATH = "/api/import"
    }

    @PostMapping(START_PATH)
    fun startGeneratingExportFor(): ResponseEntity<Void>

    @GetMapping(STATUS_PATH)
    fun checkIfDataIsReady(): ExportStateDto

    @GetMapping(DOWNLOAD_PATH)
    fun downloadData(): UserCompleteDataDto

    @PostMapping(IMPORT_PATH, consumes = ["multipart/form-data"])
    fun importData(
        @RequestPart("file")
        file: MultipartFile,
    ): ResponseEntity<Void>
}
