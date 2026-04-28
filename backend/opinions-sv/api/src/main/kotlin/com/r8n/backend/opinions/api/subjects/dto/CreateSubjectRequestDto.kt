package com.r8n.backend.opinions.api.subjects.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank

data class CreateSubjectRequestDto(
    @field:NotBlank
    val name: String,
    val referentName: String?,
    val address: String?,
    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    val latitude: Double?,
    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    val longitude: Double?,
)
