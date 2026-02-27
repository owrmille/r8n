package com.r8n.backend.opinions.domain

data class ComponentSection(
    val componentMark: Double?,
    val components: List<WeightedOpinionReference>,
)