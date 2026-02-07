package com.r8n.backend.gateway.controller

import com.r8n.backend.gateway.api.OpinionApi
import com.r8n.backend.gateway.api.dto.OpinionDto
import com.r8n.backend.gateway.api.dto.OpinionStatusEnumDto
import java.util.UUID
import java.time.Instant
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController
class StubController: OpinionApi {

    @GetMapping("/")
    override fun get(
        @RequestParam(name = "id", required = true) id: UUID
    ) = OpinionDto(
        id = id,
        author = UUID.randomUUID(),
        objective = listOf("objective1", "objective2"),
        subjective = listOf("subjective1", "subjective2"),
        mark = 4.5,
        trust = 0.8,
        status = OpinionStatusEnumDto.PUBLISHED,
        timestamp = Instant.now()
    )
}