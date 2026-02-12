package com.r8n.backend.gateway.controller

import com.r8n.backend.gateway.api.IncomingAccessRequestsApi
import com.r8n.backend.gateway.api.dto.access.RequestStatusEnumDto
import com.r8n.backend.gateway.stub.AccessRequestsTestDataFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/accessRequests/incoming")
class StubIncomingAccessRequestsController : IncomingAccessRequestsApi {

    @GetMapping("/all")
    override fun getAll(
        @RequestParam(required = false)
        since: Instant?,
        @RequestParam(required = false)
        status: RequestStatusEnumDto?,
    ) = listOf(
        AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
        AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
        AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
    )

    @GetMapping("/forList")
    override fun getForList(
        @RequestParam(required = true)
        listId: UUID,
        @RequestParam(required = false)
        since: Instant?,
        @RequestParam(required = false)
        status: RequestStatusEnumDto?,
    ) = listOf(
        AccessRequestsTestDataFactory.get(listId, status ?: RequestStatusEnumDto.SENT),
        AccessRequestsTestDataFactory.get(listId, status ?: RequestStatusEnumDto.SENT),
    )

    @PostMapping("/accept")
    override fun accept(
        @RequestParam(required = true)
        requestId: UUID,
    ) = AccessRequestsTestDataFactory.get(status = RequestStatusEnumDto.ACCEPTED)

    @PostMapping("/decline")
    override fun decline(
        @RequestParam(required = true)
        requestId: UUID,
    ) = AccessRequestsTestDataFactory.get(status = RequestStatusEnumDto.REJECTED)

    @PostMapping("/hide")
    override fun hide(
        @RequestParam(required = true)
        requestId: UUID,
    ) = AccessRequestsTestDataFactory.get(status = RequestStatusEnumDto.HIDDEN)

}