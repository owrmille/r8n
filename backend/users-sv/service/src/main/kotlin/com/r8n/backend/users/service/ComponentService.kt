package com.r8n.backend.opinions.service

import com.r8n.backend.mock.stub.OpinionTestDataFactory
import com.r8n.backend.opinions.domain.ComponentSection
import com.r8n.backend.opinions.domain.WeightedOpinionReference
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ComponentService {
    fun getComponentSection(parentOpinionId: UUID): ComponentSection {
        val stub = OpinionTestDataFactory.getOpinion(parentOpinionId)
        return ComponentSection(
            stub.componentMark,
            stub.components.map { WeightedOpinionReference(it.id, it.opinion, it.weight) }
        )
    }
}