package com.r8n.backend.mock.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.integration.api.OpinionListInternalApi
import com.r8n.backend.mock.stub.OpinionListTestDataFactory
import org.springframework.data.domain.PageImpl
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class StubOpinionListInternalController : OpinionListInternalApi {
    override fun getMineFull(pageable: PageRequestDto) =
        PageImpl(
            listOf(OpinionListTestDataFactory.getList(UUID.fromString("00000000-0000-0000-0000-000000000000"))),
        ).toResponse()
}