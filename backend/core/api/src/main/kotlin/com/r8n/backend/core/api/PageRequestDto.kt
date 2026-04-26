package com.r8n.backend.core.api

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class PageRequestDto(
    @field:Min(0)
    val page: Int,
    @field:Min(1)
    @field:Max(100)
    val size: Int,
    val sort: List<SortDto> = emptyList(),
)

data class SortDto(
    val property: String,
    val direction: SortDirection = SortDirection.ASC,
)

enum class SortDirection {
    ASC,
    DESC,
}
