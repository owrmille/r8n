package com.r8n.backend.opinions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.r8n.backend.mock.integration.UserClient
import com.r8n.backend.opinions.api.dto.opinion.OpinionDto
import com.r8n.backend.opinions.stub.OpinionTestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
)
@Import(TestObjectMapperConfiguration::class)
class OpinionsIntegrationTests {

    private companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:15")
            .withDatabaseName("opinions")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)
            .withInitScript("db/init-schema.sql")
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var userClient: UserClient

    @BeforeEach
    fun setUp() {
        whenever(userClient.getUserName(any())).thenReturn("username")
    }

    @Test
    @WithMockUser
    fun `get opinion works`() {
        val requestedId = "00000000-0000-0000-0000-000000000000"
        val result = mockMvc.perform(
            get("/opinions/id?id=$requestedId")
                .header("Authorization", "Bearer stub-access-token-123"),
        )
            .andExpect(status().isOk).andReturn()

        val actual: OpinionDto = objectMapper.readValue(result.response.contentAsString)
        assertEquals(OpinionTestDataFactory.getOpinion(UUID.fromString(requestedId)), actual)
    }

}

