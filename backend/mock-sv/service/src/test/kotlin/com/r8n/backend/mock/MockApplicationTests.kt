package com.r8n.backend.mock

import com.r8n.backend.mock.provider.database.SupportMessageRepository
import com.r8n.backend.mock.provider.database.SupportThreadRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ActiveProfiles("test")
@SpringBootTest
class MockApplicationTests {
    @MockitoBean
    lateinit var supportThreadRepository: SupportThreadRepository

    @MockitoBean
    lateinit var supportMessageRepository: SupportMessageRepository

    @Test
    fun contextLoads() {
    }
}