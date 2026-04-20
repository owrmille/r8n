package com.r8n.backend.opinionlists.service

import java.util.UUID

class OpinionListService(
    private val opinionListRepository: OpinionListRepository,
) {
    fun getList(listId: UUID): OpinionList {

    }
}