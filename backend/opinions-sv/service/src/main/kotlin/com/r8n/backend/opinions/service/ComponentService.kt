package com.r8n.backend.opinions.service

import com.r8n.backend.opinions.domain.ComponentSection
import com.r8n.backend.opinions.domain.WeightedOpinionReference
import com.r8n.backend.opinions.persistence.WeightedOpinionReferencePersistence
import com.r8n.backend.opinions.provider.database.OpinionRepository
import com.r8n.backend.opinions.provider.database.WeightedOpinionReferenceRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ComponentService(
    private val weightedOpinionReferenceRepository: WeightedOpinionReferenceRepository,
    private val opinionRepository: OpinionRepository,
) {
    fun linkComponent(
        parentOpinionId: UUID,
        childOpinionId: UUID,
        weight: Double,
    ) {
        weightedOpinionReferenceRepository.save(
            WeightedOpinionReferencePersistence(
                parentOpinion = parentOpinionId,
                childOpinion = childOpinionId,
                weight = weight,
            ),
        )
    }

    fun getComponentSection(parentOpinionId: UUID): ComponentSection {
        val components =
            weightedOpinionReferenceRepository
                .findAllByParentOpinionOrderByIdAsc(parentOpinionId)
                .map { WeightedOpinionReference(it.id!!, it.childOpinion, it.weight) }
        val childMarksById =
            opinionRepository
                .findAllById(components.map { it.opinion }.distinct())
                .associate { it.id!! to it.mark }
        val weightedMarks =
            components.mapNotNull { component ->
                childMarksById[component.opinion]?.let { mark -> component.weight * mark }
            }
        val componentMark = weightedMarks.takeIf { it.isNotEmpty() && it.size == components.size }?.sum()
        return ComponentSection(
            componentMark,
            components,
        )
    }
}