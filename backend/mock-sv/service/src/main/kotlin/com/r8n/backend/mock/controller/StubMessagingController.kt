package com.r8n.backend.mock.controller

import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.stub.MiscTestFactory
import org.springframework.data.domain.PageImpl
import org.springframework.web.bind.annotation.RestController

@RestController
class StubMessagingController : MessagingApi {
    override fun getSupportThreads() =
        PageImpl(
            listOf(MiscTestFactory.getSupportMessage()),
        ).toResponse()
}