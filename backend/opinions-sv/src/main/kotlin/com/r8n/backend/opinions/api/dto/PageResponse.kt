package com.r8n.backend.opinions.api.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val size: Int
)

fun <T : Any> Page<T>.toResponse(): PageResponse<T> =
    PageResponse(
        items = content,
        total = totalElements,
        page = number,
        size = size
    )