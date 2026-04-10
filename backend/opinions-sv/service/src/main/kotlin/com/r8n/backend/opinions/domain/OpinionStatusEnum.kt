package com.r8n.backend.opinions.domain

import com.r8n.backend.opinions.api.dto.OpinionStatusEnumDto

enum class OpinionStatusEnum {
    DRAFT,
    PENDING_PREMODERATION,
    PUBLISHED,
    REJECTED,
}

fun OpinionStatusEnum.toDto(): OpinionStatusEnumDto =
    when (this) {
        OpinionStatusEnum.DRAFT -> OpinionStatusEnumDto.DRAFT
        OpinionStatusEnum.PENDING_PREMODERATION -> OpinionStatusEnumDto.PENDING_PREMODERATION
        OpinionStatusEnum.PUBLISHED -> OpinionStatusEnumDto.PUBLISHED
        OpinionStatusEnum.REJECTED -> OpinionStatusEnumDto.REJECTED
    }

fun OpinionStatusEnumDto.fromDto(): OpinionStatusEnum =
    when (this) {
        OpinionStatusEnumDto.DRAFT -> OpinionStatusEnum.DRAFT
        OpinionStatusEnumDto.PENDING_PREMODERATION -> OpinionStatusEnum.PENDING_PREMODERATION
        OpinionStatusEnumDto.PUBLISHED -> OpinionStatusEnum.PUBLISHED
        OpinionStatusEnumDto.REJECTED -> OpinionStatusEnum.REJECTED
    }