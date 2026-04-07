package com.r8n.backend.core.api

data class PageRequestDto(
    val page: Int,
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