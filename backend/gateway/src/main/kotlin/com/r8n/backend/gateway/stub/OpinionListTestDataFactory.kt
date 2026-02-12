package com.r8n.backend.gateway.stub

import com.r8n.backend.gateway.api.dto.list.OpinionListDto
import com.r8n.backend.gateway.api.dto.list.OpinionListPrivacyEnumDto
import com.r8n.backend.gateway.api.dto.list.OpinionListSummaryDto
import com.r8n.backend.gateway.api.dto.opinion.OpinionSummaryDto
import java.util.UUID

object OpinionListTestDataFactory {
    fun getListSummary(id: UUID = UUID.randomUUID()) = OpinionListSummaryDto(
        id,
        "${OpinionTestDataFactory.bernard.name}'s cappucino rating",
        OpinionTestDataFactory.bernard.id,
        OpinionTestDataFactory.bernard.name,
        3,
        1,
        OpinionListPrivacyEnumDto.SEARCHABLE,
    )

    fun getOpinionSummary(): OpinionSummaryDto {
        val list = listOf(OpinionTestDataFactory.bernardOnCap1(), OpinionTestDataFactory.donaldOnCap1())
        val res = OpinionSummaryDto(
            UUID.randomUUID(),
            OpinionTestDataFactory.cappuccino1.id,
            OpinionTestDataFactory.cappuccino1.name,
            OpinionTestDataFactory.bernardOnCap1().mark,
            0.0,
            0.0,

        )
    }

    fun getList(id: UUID = UUID.randomUUID()) = OpinionListDto(
        id,
        "${OpinionTestDataFactory.bernard.name}'s cappucino rating",
        OpinionTestDataFactory.bernard.id,
        OpinionTestDataFactory.bernard.name,
        listOf(
        ),
    )
}