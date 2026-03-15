package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.domain.ComponentSection
import com.r8n.backend.opinions.domain.WeightedOpinionReference
import com.r8n.backend.opinions.provider.database.WeightedOpinionReferenceRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ComponentService(
    private val weightedOpinionReferenceRepository: WeightedOpinionReferenceRepository,
) {
    fun getComponentSection(parentOpinionId: UUID): ComponentSection {
        val components = weightedOpinionReferenceRepository.findAllByParentOpinionOrderByIdAsc(parentOpinionId)
            .map { WeightedOpinionReference(it.id, it.childOpinion, it.weight) }
        val componentMark = components.takeIf { it.isNotEmpty() }?.sumOf { it.weight }
        return ComponentSection(
            componentMark,
            components,
        )
    }
}
