package com.r8n.backend.gateway.controller

import com.r8n.backend.gateway.api.IncomingAccessRequestsApi
import com.r8n.backend.gateway.api.dto.access.RequestStatusEnumDto
import com.r8n.backend.gateway.api.dto.toResponse
import com.r8n.backend.gateway.stub.AccessRequestsTestDataFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/accessRequests/incoming")
class StubIncomingAccessRequestsController : IncomingAccessRequestsApi {

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
        )
    ).toResponse()

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