package com.r8n.backend.opinions.service

import com.r8n.backend.mock.stub.OpinionSubjectTestDataFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubjectService {
    private val subjectNamesById = listOf(
        OpinionSubjectTestDataFactory.cafeEinsA,
        OpinionSubjectTestDataFactory.cafeZwei,
        OpinionSubjectTestDataFactory.cafeDrei,
        OpinionSubjectTestDataFactory.alexander,
        OpinionSubjectTestDataFactory.bernard,
        OpinionSubjectTestDataFactory.cappuccino1A,
        OpinionSubjectTestDataFactory.cappuccino1G,
        OpinionSubjectTestDataFactory.cappuccino1Athicc,
        OpinionSubjectTestDataFactory.cappuccino2,
        OpinionSubjectTestDataFactory.cappuccino3
    ).associate { it.id to it.name }

    fun getSubjectName(id: UUID) = subjectNamesById[id] ?: "Subject"
}
