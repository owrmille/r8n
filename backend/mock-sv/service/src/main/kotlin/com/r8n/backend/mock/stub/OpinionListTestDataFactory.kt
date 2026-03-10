package com.r8n.backend.mock.stub

import com.r8n.backend.mock.api.dto.list.OpinionListDto
import com.r8n.backend.mock.api.dto.list.OpinionListPrivacyEnumDto
import com.r8n.backend.mock.api.dto.list.OpinionListSummaryDto
import com.r8n.backend.opinions.api.dto.opinion.OpinionSummaryDto
import com.r8n.backend.opinions.api.dto.opinion.WeightedOpinionReferenceDto
import com.r8n.backend.mock.stub.OpinionSubjectTestDataFactory.bernard
import java.util.UUID

object OpinionListTestDataFactory {
    fun getListSummary(id: UUID = UUID.randomUUID()) = OpinionListSummaryDto(
        id,
        "${bernard.name}'s cappucino rating",
        bernard.id,
        bernard.name,
        3,
        1,
        OpinionListPrivacyEnumDto.SEARCHABLE,
    )

    fun getOpinionSummary1() = OpinionSummaryDto(
        UUID.randomUUID(),
        OpinionTestDataFactory.bernardOnCap1().subject,
        OpinionTestDataFactory.bernardOnCap1().subjectName,
        OpinionTestDataFactory.bernardOnCap1().mark,
        3.0,
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

    fun getOpinionSummary3() = OpinionSummaryDto(
        UUID.randomUUID(),
        OpinionTestDataFactory.bernardOnCap3().subject,
        OpinionTestDataFactory.bernardOnCap3().subjectName,
        OpinionTestDataFactory.bernardOnCap3().mark,
        4.3,
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
        val opinionListDto = OpinionListDto(
            id,
            "${bernard.name}'s cappucino rating",
            bernard.id,
            bernard.name,
            listOf(getOpinionSummary1(), getOpinionSummary3()),
        )
        return opinionListDto
    }
}