package com.r8n.backend.access

import com.r8n.backend.mock.api.OpinionListApi
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ActiveProfiles("test")
@SpringBootTest
class AccessApplicationTests {
    @MockitoBean
    lateinit var opinionListApi: OpinionListApi

    @MockitoBean
    lateinit var usersInternalApi: UsersInternalApi

    @Test
    fun contextLoads() {
    }
}