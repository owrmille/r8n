package com.r8n.backend.export.api

import com.r8n.backend.export.api.dto.ExportStateDto
import com.r8n.backend.export.api.dto.UserCompleteDataDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

interface ExportApi {
    companion object {
        private const val BASE_PATH = "/api/export"
        const val START_PATH = "$BASE_PATH/start"
        const val STATUS_PATH = "$BASE_PATH/status"
        const val DOWNLOAD_PATH = "$BASE_PATH/download"
    }

    @PostMapping(START_PATH)
    fun startGeneratingExportFor(): ResponseEntity<Void>

    @GetMapping(STATUS_PATH)
    fun checkIfDataIsReady(): ExportStateDto

    @GetMapping(DOWNLOAD_PATH)
    fun downloadData(): UserCompleteDataDto
}
