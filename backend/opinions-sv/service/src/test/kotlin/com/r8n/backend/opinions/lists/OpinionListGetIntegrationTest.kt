package com.r8n.backend.opinions.lists

import com.r8n.backend.opinions.TestObjectMapperConfiguration
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListSummaryDto
import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.util.UUID

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Import(TestObjectMapperConfiguration::class)
class OpinionListGetIntegrationTest {
    private companion object {
        val ANNA_ID: UUID = UUID.fromString("20202020-2020-2020-2020-202020202020")

        val ANNA_L11_ID: UUID = UUID.fromString("80000000-0000-0000-0000-000000000111")

        @Suppress("unused")
        @Container
        @ServiceConnection
        val postgres: PostgreSQLContainer =
            PostgreSQLContainer(DockerImageName.parse("postgres:15"))
                .withDatabaseName("opinions")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("db/init-schema.sql")
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: JsonMapper

    @Autowired
    lateinit var serviceTokenService: ServiceTokenService

    @MockitoBean
    lateinit var usersInternalApi: UsersInternalApi

    lateinit var annaToken: String

    @BeforeEach
    fun setUp() {
        annaToken = "Bearer " + serviceTokenService.generateAccessToken(ANNA_ID, listOf("USER"))
        whenever(usersInternalApi.isAnyModerator(any())).thenReturn(false)
        whenever(usersInternalApi.isHumanModerator(any())).thenReturn(false)
        whenever(usersInternalApi.getUserName(any())).thenReturn("Anna Müller")
    }

    @Test
    fun `publishedAfter before all opinions returns all summaries`() {
        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists/$ANNA_L11_ID")
                        .header("Authorization", annaToken)
                        .param("publishedAfter", "2024-01-01T00:00:00Z"),
                ).andExpect(status().isOk)
                .andReturn()

        val list = objectMapper.readValue<OpinionListDto>(result.response.contentAsString)
        // l11 has r11 (s1), r12 (s2) directly, plus synced r23 (s3), r24 (s4), r31 (s1 duplicate — deduped)
        assertThat(list.opinionSummaries).hasSize(4)
    }

    @Test
    fun `publishedAfter after all opinions returns empty summaries`() {
        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists/$ANNA_L11_ID")
                        .header("Authorization", annaToken)
                        .param("publishedAfter", "2025-01-01T00:00:00Z"),
                ).andExpect(status().isOk)
                .andReturn()

        val list = objectMapper.readValue<OpinionListDto>(result.response.contentAsString)
        assertThat(list.opinionSummaries).isEmpty()
    }

    @Test
    fun `getMine returns all lists owned by the authenticated user`() {
        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists/mine")
                        .header("Authorization", annaToken)
                        .param("page", "0")
                        .param("size", "20"),
                ).andExpect(status().isOk)
                .andReturn()

        val page = objectMapper.readValue<PageResponseDto<OpinionListSummaryDto>>(result.response.contentAsString)
        // Anna owns l11, l12, l13 plus two lists from other seed changesets
        assertThat(page.total).isEqualTo(5)
        assertThat(page.items.map { it.listName }).contains("l11", "l12", "l13")
    }

    @Test
    fun `getListSummary returns correct opinion count and list metadata`() {
        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists/$ANNA_L11_ID/summary")
                        .header("Authorization", annaToken),
                ).andExpect(status().isOk)
                .andReturn()

        val summary = objectMapper.readValue<OpinionListSummaryDto>(result.response.contentAsString)
        assertThat(summary.listId).isEqualTo(ANNA_L11_ID)
        assertThat(summary.listName).isEqualTo("l11")
        // l11 has 2 directly linked opinions (r11, r12)
        assertThat(summary.opinionsCount).isEqualTo(2)
    }
}
