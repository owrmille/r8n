package com.r8n.backend.mock.controller

import com.r8n.backend.core.api.PageRequestDto
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.api.SelectorApi
import com.r8n.backend.mock.api.dto.about.SelectorDto
import com.r8n.backend.mock.stub.MiscTestFactory
import com.r8n.backend.mock.stub.SelectorTestDataFactory
import org.springframework.data.domain.PageImpl
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class StubSelectorController : SelectorApi {
    override fun getForURL(
        url: String,
        pageable: PageRequestDto,
    ) = PageImpl(listOf(SelectorTestDataFactory.getSelector())).toResponse()

    override fun getForSubject(
        subjectId: UUID,
        pageable: PageRequestDto,
    ) = PageImpl(listOf(SelectorTestDataFactory.getSelector())).toResponse()

    override fun suggest(
        subjectId: UUID,
        selector: String,
    ): SelectorDto = SelectorTestDataFactory.getSelector()

    override fun disagree(
        selectorId: UUID,
        comment: String?,
    ) = MiscTestFactory.getSupportMessage()
}