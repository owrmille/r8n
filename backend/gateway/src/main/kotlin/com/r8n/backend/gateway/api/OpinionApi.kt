package com.r8n.backend.gateway.api

import com.r8n.backend.gateway.api.dto.OpinionDto
import java.util.UUID

interface OpinionApi {
	fun get(id: UUID): OpinionDto
}