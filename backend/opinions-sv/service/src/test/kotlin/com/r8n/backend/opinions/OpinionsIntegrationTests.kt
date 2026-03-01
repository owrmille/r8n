package com.r8n.backend.opinions

import com.r8n.backend.mock.integration.UserClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.mockito.ArgumentMatchers.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpinionsIntegrationTests {

    @Container
    @ServiceConnection
    val postgres = PostgreSQLContainer("postgres:16")
        .withDatabaseName("opinions")
        .withUsername("test")
        .withPassword("test")

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var userClient: UserClient

    @BeforeEach
    fun setUp() {
        whenever(userClient.getUserName(any())).thenReturn("username")
    }

    @Test
    fun `get opinion works`() {
        mockMvc.perform(get("/opinions/id?id=00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isOk)
    }

}

