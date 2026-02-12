package com.r8n.backend.gateway.controller

import com.r8n.backend.gateway.api.OpinionListApi
import com.r8n.backend.gateway.api.dto.list.OpinionListSummaryDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/opinionLists")
class StubOpinionListController : OpinionListApi {

    @GetMapping("/summary")
    override fun getListSummary(
        @RequestParam(required = true)
        listId: UUID
    ): OpinionListSummaryDto {
        TODO("Not yet implemented")
    }

}