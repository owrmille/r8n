package com.r8n.backend.opinionlists.domain

import java.util.UUID

data class OpinionList(
    val id: UUID,
    val owner: UUID,
    val privacy: OpinionListPrivacyEnum,
    val opinions: List<Opinion>,
    )