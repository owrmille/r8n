package com.r8n.backend.gateway.stub

import com.r8n.backend.gateway.api.dto.opinion.OpinionDto
import com.r8n.backend.gateway.api.dto.opinion.OpinionStatusEnumDto
import com.r8n.backend.gateway.api.dto.opinion.OpinionSummaryDto
import com.r8n.backend.gateway.api.dto.opinion.WeightedOpinionReferenceDto
import java.time.Instant
import java.util.UUID

object OpinionTestDataFactory {
    data class Identifiable(val id: UUID, val name: String)

    val alexander = Identifiable(
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        "Alexander",
    )
    val bernard = Identifiable(
        UUID.fromString("01010101-0101-0101-0101-010101010101"),
        "Bernard",
    )
    val cappuccino1 = Identifiable(
        UUID.fromString("02020202-0202-0202-0202-020202020202"),
        "cappuccino @ Cafe Eins",
    )
    val cappuccino2 = Identifiable(
        UUID.fromString("03030303-0303-0303-0303-030303030303"),
        "cappuccino extra thicc @ Cafe Eins",
    )
    val cappuccino3 = Identifiable(
        UUID.fromString("04040404-0404-0404-0404-040404040404"),
        "cappuccino @ Cafe Zwei",
    )
    val donald = Identifiable(
        UUID.fromString("05050505-0505-0505-0505-050505050505"),
        "Donald",
    )
    val cafeEins = Identifiable(
        UUID.fromString("06060606-0606-0606-0606-060606060606"),
        "Cafe Eins",
    )
    val venezuelaInvasion = Identifiable(
        UUID.fromString("07070707-0707-0707-0707-070707070707"),
        "ordered Nicholas Maduro to be kidnapped",
    )
    val canadaTariffs = Identifiable(
        UUID.fromString("08080808-0808-0808-0808-080808080808"),
        "imposed trade tariffs on Canada",
    )
    val moreMoney = Identifiable(
        UUID.fromString("09090909-0909-0909-0909-090909090909"),
        "USA gets more money from Canada",
    )
    val brokenTrust = Identifiable(
        UUID.fromString("10101010-1010-1010-1010-101010101010"),
        "nobody wants to trade with USA anymore",
    )

    fun alexanderOnDonald(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        alexander.id,
        alexander.name,
        donald.id,
        donald.name,
        listOf(),
        listOf("born 1946"),
        mark = 1.07,
        componentMark = 1.00,
        components = listOf(
            WeightedOpinionReferenceDto(UUID.randomUUID(), venezuelaInvasion.id, 0.8),
            WeightedOpinionReferenceDto(UUID.randomUUID(), canadaTariffs.id, 1.0),
        ),
        OpinionStatusEnumDto.DRAFT,
        Instant.now(),
    )

    fun alexanderOnVenezuela(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        alexander.id,
        alexander.name,
        venezuelaInvasion.id,
        venezuelaInvasion.name,
        listOf("you can't just kidnap people"),
        listOf("03.01.2026"),
        2.50,
        null,
        emptyList(),
        OpinionStatusEnumDto.PUBLISHED,
        Instant.now(),
    )

    fun alexanderOnTariffs(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        alexander.id,
        alexander.name,
        canadaTariffs.id,
        canadaTariffs.name,
        emptyList(),
        emptyList(),
        null,
        0.00,
        listOf(
            WeightedOpinionReferenceDto(UUID.randomUUID(), moreMoney.id, 1.0),
            WeightedOpinionReferenceDto(UUID.randomUUID(), brokenTrust.id, 0.3),
        ),
        OpinionStatusEnumDto.PENDING_PREMODERATION,
        Instant.now(),
    )

    fun alexanderOnMoney(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        alexander.id,
        alexander.name,
        moreMoney.id,
        moreMoney.name,
        emptyList(),
        emptyList(),
        5.0,
        null,
        emptyList(),
        OpinionStatusEnumDto.PUBLISHED,
        Instant.now(),
    )

    fun alexanderOnTrust(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        alexander.id,
        alexander.name,
        brokenTrust.id,
        brokenTrust.name,
        emptyList(),
        emptyList(),
        2.0,
        null,
        emptyList(),
        OpinionStatusEnumDto.PUBLISHED,
        Instant.now(),
    )

    fun bernardOnCap1(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        bernard.id,
        bernard.name,
        cappuccino1.id,
        cappuccino1.name,
        listOf("reminds of grandma's home coffee"),
        listOf("5.50€", "lactose-free milk"),
        4.23,
        null,
        emptyList(),
        OpinionStatusEnumDto.DRAFT,
        Instant.now(),
    )

    fun bernardOnCap2(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        bernard.id,
        bernard.name,
        cappuccino2.id,
        cappuccino2.name,
        listOf("so thiccccccc"),
        listOf("6.60€"),
        4.69,
        null,
        emptyList(),
        OpinionStatusEnumDto.PUBLISHED,
        Instant.now(),
    )

    fun bernardOnCap3(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        bernard.id,
        bernard.name,
        cappuccino3.id,
        cappuccino3.name,
        listOf("my favorite"),
        listOf("5.75€"),
        4.90,
        null,
        emptyList(),
        OpinionStatusEnumDto.PUBLISHED,
        Instant.now(),
    )

    fun alexanderOnCap1(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        alexander.id,
        alexander.name,
        cappuccino1.id,
        cappuccino1.name,
        emptyList(),
        listOf("how the hell do you call this coffee? this is piss"),
        1.0,
        null,
        emptyList(),
        OpinionStatusEnumDto.REJECTED,
        Instant.now(),
    )

    fun donaldOnCap1(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        donald.id,
        donald.name,
        cappuccino1.id,
        cappuccino1.name,
        emptyList(),
        listOf(
            "nobody knows more about cappuccino than I do",
            "Make Caffeine Great Again",
            "Thank you for your attention to the matter",
        ),
        5.00,
        null,
        emptyList(),
        OpinionStatusEnumDto.PUBLISHED,
        Instant.now(),
    )

    fun donaldOnCap3(id: UUID = UUID.randomUUID()) = OpinionDto(
        id,
        donald.id,
        donald.name,
        cappuccino3.id,
        cappuccino3.name,
        emptyList(),
        listOf(
            "coffee is a democrat hoax",
            "so this coffee is why Joe was so sleepy",
        ),
        0.00,
        null,
        emptyList(),
        OpinionStatusEnumDto.PUBLISHED,
        Instant.now(),
    )

    fun getOpinion(id: UUID) = when (id.mostSignificantBits % 11) {
        0L -> alexanderOnDonald(id)
        1L -> alexanderOnVenezuela(id)
        2L -> alexanderOnTariffs(id)
        3L -> alexanderOnMoney(id)
        4L -> alexanderOnTrust(id)
        5L -> bernardOnCap1(id)
        6L -> bernardOnCap2(id)
        7L -> bernardOnCap3(id)
        8L -> donaldOnCap1(id)
        9L -> donaldOnCap3(id)
        else -> alexanderOnCap1(id)
    }

    fun postOpinion(
        opinionId: UUID? = null,
        subjectId: UUID? = null,
        subjective: List<String>,
        objective: List<String>,
        mark: Double?,
    ) = OpinionDto(
        opinionId ?: UUID.randomUUID(),
        UUID.randomUUID(),
        "YourName",
        subjectId ?: UUID.randomUUID(),
        "the famous $subjectId",
        subjective,
        objective,
        mark,
        null,
        emptyList(),
        OpinionStatusEnumDto.PENDING_PREMODERATION,
        Instant.now(),
    )
}