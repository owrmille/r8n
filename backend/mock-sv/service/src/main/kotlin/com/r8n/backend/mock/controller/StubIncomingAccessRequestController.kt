package com.r8n.backend.mock.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.api.IncomingAccessRequestApi
import com.r8n.backend.mock.api.dto.access.RequestStatusEnumDto
import com.r8n.backend.mock.stub.AccessRequestsTestDataFactory
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.PageImpl
import java.time.Instant
import java.util.UUID

@RestController
class StubIncomingAccessRequestController : IncomingAccessRequestApi {

    override fun get(
        forListId: UUID?,
        since: Instant?,
        status: RequestStatusEnumDto?,
        pageable: PageRequestDto,
    ) = PageImpl(
            listOf(
                AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
                AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
                AccessRequestsTestDataFactory.get(status = status ?: RequestStatusEnumDto.SENT),
            )
        ).toResponse()

    override fun accept(
        requestId: UUID,
    ) = AccessRequestsTestDataFactory.get(status = RequestStatusEnumDto.ACCEPTED)

    override fun decline(
        requestId: UUID,
    ) = AccessRequestsTestDataFactory.get(status = RequestStatusEnumDto.REJECTED)

    override fun hide(
        requestId: UUID,
    ) = AccessRequestsTestDataFactory.get(status = RequestStatusEnumDto.HIDDEN)

}