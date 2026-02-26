package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.stub.OpinionTestDataFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OpinionService {
    fun getOpinion(id: UUID) = OpinionTestDataFactory.getOpinion(id)
}