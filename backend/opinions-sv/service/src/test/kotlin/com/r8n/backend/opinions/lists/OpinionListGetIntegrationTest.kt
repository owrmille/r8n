package com.r8n.backend.opinions.lists

import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.TestObjectMapperConfiguration
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListSummaryDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
        val SUBJECT_1_ID: UUID = UUID.fromString("10000000-0000-0000-0000-000000000001")

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
    fun `getMine returns all lists owned by the authenticated user including virtual list`() {
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
        // Anna owns 5 lists in seed data + 1 virtual list
        assertThat(page.items.map { it.listName }).contains("[ALL]", "l11", "l12", "l13")
        val virtualList = page.items.find { it.listName == "[ALL]" }
        assertThat(virtualList).isNotNull
        assertThat(virtualList?.listId).isNull()
        // Anna has multiple opinions in seed data (r11, r12, r21, r22, r31, etc.)
        assertThat(virtualList?.opinionsCount).isGreaterThan(0)
    }

    @Test
    fun `getList with null listId returns virtual list`() {
        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists")
                        .header("Authorization", annaToken),
                ).andExpect(status().isOk)
                .andReturn()

        val list = objectMapper.readValue<OpinionListDto>(result.response.contentAsString)
        assertThat(list.listName).isEqualTo("[ALL]")
        assertThat(list.id).isNull()
        assertThat(list.owner).isEqualTo(ANNA_ID)
    }

    @Test
    fun `getListSummary with null listId returns virtual list summary`() {
        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists/summary")
                        .header("Authorization", annaToken),
                ).andExpect(status().isOk)
                .andReturn()

        val summary = objectMapper.readValue<OpinionListSummaryDto>(result.response.contentAsString)
        assertThat(summary.listName).isEqualTo("[ALL]")
        assertThat(summary.listId).isNull()
        assertThat(summary.owner).isEqualTo(ANNA_ID)
    }

    @Test
    fun `getListsFull returns all lists with full details including virtual list`() {
        val result =
            mockMvc
                .perform(
                    get("/api/internal/opinion-lists/mine/full")
                        .header("Authorization", annaToken)
                        .param("page", "0")
                        .param("size", "20"),
                ).andExpect(status().isOk)
                .andReturn()

        val page = objectMapper.readValue<PageResponseDto<OpinionListDto>>(result.response.contentAsString)
        // Anna owns 5 lists in seed data + 1 virtual list
        assertThat(page.items.map { it.listName }).contains("[ALL]", "l11", "l12", "l13")
        val virtualList = page.items.find { it.listName == "[ALL]" }
        assertThat(virtualList).isNotNull
        assertThat(virtualList?.id).isNull()
        assertThat(virtualList?.opinionSummaries).isNotEmpty
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

    @Test
    fun `search returns only accessible lists matching name substring`() {
        val user2Id = UUID.fromString("40404040-4040-4040-4040-404040404040")
        val user2Token = "Bearer " + serviceTokenService.generateAccessToken(user2Id, listOf("USER"))
        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists/search")
                        .header("Authorization", user2Token)
                        .param("nameSubstring", "l11")
                        .param("page", "0")
                        .param("size", "20"),
                ).andExpect(status().isOk)
                .andReturn()

        val page = objectMapper.readValue<PageResponseDto<OpinionListSummaryDto>>(result.response.contentAsString)
        assertThat(page.items.map { it.listName }).contains("l11")
        assertThat(page.items).allSatisfy { it.listName.contains("l11") }
    }

    @Test
    fun `weighted rating uses stored weights for owners own opinions`() {
        val createdList =
            mockMvc
                .perform(
                    post("/api/opinion-lists")
                        .header("Authorization", annaToken)
                        .param("name", "weighted-own-opinions")
                        .param("privacy", "PRIVATE"),
                ).andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
                .let { objectMapper.readValue<OpinionListDto>(it) }

        val firstOpinion =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .header("Authorization", annaToken)
                        .param("subjectId", SUBJECT_1_ID.toString())
                        .param("subjective", "first weighted opinion")
                        .param("objective", "objective one")
                        .param("mark", "6.0"),
                ).andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
                .let { objectMapper.readValue<OpinionDto>(it) }

        val secondOpinion =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .header("Authorization", annaToken)
                        .param("subjectId", SUBJECT_1_ID.toString())
                        .param("subjective", "second weighted opinion")
                        .param("objective", "objective two")
                        .param("mark", "4.0"),
                ).andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
                .let { objectMapper.readValue<OpinionDto>(it) }

        mockMvc
            .perform(
                post("/api/opinion-lists/${createdList.id}/link")
                    .header("Authorization", annaToken)
                    .param("opinionId", firstOpinion.id.toString())
                    .param("weight", "0.7"),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/api/opinion-lists/${createdList.id}/link")
                    .header("Authorization", annaToken)
                    .param("opinionId", secondOpinion.id.toString())
                    .param("weight", "1.0"),
            ).andExpect(status().isOk)

        val fetchedList =
            mockMvc
                .perform(
                    get("/api/opinion-lists/${createdList.id}")
                        .header("Authorization", annaToken),
                ).andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
                .let { objectMapper.readValue<OpinionListDto>(it) }

        val summary = fetchedList.opinionSummaries.single()
        assertThat(summary.opinions).hasSize(2)
        assertThat(summary.opinions.map { it.weight }).containsExactlyInAnyOrder(0.7, 1.0)
        assertThat(summary.componentMark).isCloseTo(4.823529411764706, within(1e-12))
    }
}
