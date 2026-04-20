package com.r8n.backend.opinions.opinions.domain

import com.r8n.backend.opinions.api.opinions.dto.OpinionStatusEnumDto

enum class OpinionStatusEnum {
    DRAFT,
    PENDING_PREMODERATION,
    PUBLISHED,
    REJECTED,
}
