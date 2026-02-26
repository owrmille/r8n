package com.r8n.backend.mock.controller

import com.r8n.backend.mock.api.OutgoingAccessRequestsApi
import com.r8n.backend.mock.api.dto.access.RequestStatusEnumDto
import com.r8n.backend.mock.api.dto.toResponse
import com.r8n.backend.mock.stub.AccessRequestsTestDataFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/accessRequests/outgoing")
class StubOutgoingAccessRequestsController : OutgoingAccessRequestsApi {

    @GetMapping("/get")
    override fun get(
        @RequestParam(required = false)
        forListId: UUID?,
        @RequestParam(required = false)
        since: Instant?,
        @RequestParam(required = false)
        status: RequestStatusEnumDto?,
        pageable: Pageable,
    ) = PageImpl(
        listOf(
            AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
            AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
            AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
        ),
    ).toResponse()

    @PostMapping("/create")
    override fun create(
        @RequestParam(required = true)
        listId: UUID,
    ) = AccessRequestsTestDataFactory.get(status = RequestStatusEnumDto.SENT)

    @PostMapping("/cancel")
    override fun cancel(
        @RequestParam(required = true)
        requestId: UUID,
    ) = AccessRequestsTestDataFactory.get(status = RequestStatusEnumDto.CANCELLED)
}