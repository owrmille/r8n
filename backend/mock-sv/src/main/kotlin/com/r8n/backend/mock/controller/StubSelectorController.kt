package com.r8n.backend.mock.controller

import com.r8n.backend.mock.api.SelectorApi
import com.r8n.backend.mock.api.dto.toResponse
import com.r8n.backend.mock.stub.MiscTestFactory
import com.r8n.backend.mock.stub.SelectorTestDataFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/selectors")
class StubSelectorController : SelectorApi {
    @GetMapping("/forUrl")
    override fun getForURL(
        @RequestParam(required = true)
        url: String,
        pageable: Pageable,
    ) = PageImpl(listOf(SelectorTestDataFactory.getSelector())).toResponse()

    @GetMapping("/forSubject")
    override fun getForSubject(
        @RequestParam(required = true)
        id: UUID,
        pageable: Pageable,
    ) = PageImpl(listOf(SelectorTestDataFactory.getSelector())).toResponse()

    @PostMapping("/suggest")
    override fun suggest(
        @RequestParam(required = true)
        subjectId: UUID,
        @RequestParam(required = true)
        selector: String,
    ) = SelectorTestDataFactory.getSelector()

    @PatchMapping("/disagree")
    override fun disagree(
        @RequestParam(required = true)
        selectorId: UUID,
        @RequestParam(required = false)
        comment: String?,
    ) = MiscTestFactory.getSupportMessage()
}