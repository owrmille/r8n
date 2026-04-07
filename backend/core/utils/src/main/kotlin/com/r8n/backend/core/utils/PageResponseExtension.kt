package com.r8n.backend.core.utils

import com.r8n.backend.core.api.PageResponseDto
import org.springframework.data.domain.Page

fun <T : Any> Page<T>.toResponse(): PageResponseDto<T> =
    PageResponseDto(
        items = content,
        total = totalElements,
        page = number,
        size = size,
    )