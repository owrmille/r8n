package com.r8n.backend.opinions.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OpinionNoteService {
    fun getSubjective(id: UUID) = listOf("subjective 1", "subjective 2")
    fun getObjective(id: UUID) = listOf("objective 1", "objective 2")
}