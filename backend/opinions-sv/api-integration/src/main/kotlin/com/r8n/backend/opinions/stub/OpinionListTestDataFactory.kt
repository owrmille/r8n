package com.r8n.backend.opinions.stub

import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListPrivacyEnumDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListSummaryDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionSummaryDto
import com.r8n.backend.opinions.api.opinions.dto.WeightedOpinionReferenceDto
import java.util.UUID

object OpinionListTestDataFactory {
    fun getListSummary(id: UUID = UUID.randomUUID()) =
        OpinionListSummaryDto(
            id,
            "${OpinionSubjectTestDataFactory.bernard.name}'s cappuccino rating",
            OpinionSubjectTestDataFactory.bernard.id,
            OpinionSubjectTestDataFactory.bernard.name,
            3,
            1,
            OpinionListPrivacyEnumDto.SEARCHABLE,
        )

    fun getOpinionSummary1() =
        OpinionSummaryDto(
            OpinionTestDataFactory.bernardOnCap1().subject,
            OpinionTestDataFactory.bernardOnCap1().subjectName,
            OpinionTestDataFactory.bernardOnCap1().mark,
            OpinionTestDataFactory.bernardOnCap1().componentMark,
            listOf(
                WeightedOpinionReferenceDto(
                    UUID.randomUUID(),
                    OpinionTestDataFactory.bernardOnCap1().id,
                    1.0,
                ),
                WeightedOpinionReferenceDto(
                    UUID.randomUUID(),
                    OpinionTestDataFactory.donaldOnCap1().id,
                    0.3,
                ),
            ),
        )

    fun getOpinionSummary3() =
        OpinionSummaryDto(
            OpinionTestDataFactory.bernardOnCap3().subject,
            OpinionTestDataFactory.bernardOnCap3().subjectName,
            OpinionTestDataFactory.bernardOnCap3().mark,
            OpinionTestDataFactory.bernardOnCap3().componentMark,
            listOf(
                WeightedOpinionReferenceDto(
                    UUID.randomUUID(),
                    OpinionTestDataFactory.bernardOnCap3().id,
                    1.0,
                ),
                WeightedOpinionReferenceDto(
                    UUID.randomUUID(),
                    OpinionTestDataFactory.donaldOnCap3().id,
                    0.1,
                ),
            ),
        )

    fun getList(id: UUID = UUID.randomUUID()): OpinionListDto {
        val opinionListDto =
            OpinionListDto(
                id,
                "${OpinionSubjectTestDataFactory.bernard.name}'s cappuccino rating",
                OpinionSubjectTestDataFactory.bernard.id,
                OpinionSubjectTestDataFactory.bernard.name,
                listOf(getOpinionSummary1(), getOpinionSummary3()),
                OpinionListPrivacyEnumDto.SEARCHABLE,
            )
        return opinionListDto
    }
}