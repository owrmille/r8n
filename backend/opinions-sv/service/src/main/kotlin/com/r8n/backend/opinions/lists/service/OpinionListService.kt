package com.r8n.backend.opinions.lists.service

import com.r8n.backend.opinions.lists.domain.OpinionList
import java.util.UUID

class OpinionListService(
    private val opinionListRepository: OpinionListRepository,
) {
    fun getList(listId: UUID): OpinionList {

    }
}