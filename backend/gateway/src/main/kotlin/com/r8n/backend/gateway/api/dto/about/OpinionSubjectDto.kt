package com.r8n.backend.gateway.api.dto.about

import java.util.UUID

class OpinionSubjectDto (
    val id: UUID,
    val name: String,
    val primaryReferent: ReferentDto,
    val alternativeReferents: List<ReferentDto>,
    val links: List<String>,
)