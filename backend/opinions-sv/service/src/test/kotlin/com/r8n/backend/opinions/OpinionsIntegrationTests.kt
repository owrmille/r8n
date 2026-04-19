package com.r8n.backend.opinions

import com.r8n.backend.mock.stub.OpinionSubjectTestDataFactory.bernardReferent
import com.r8n.backend.mock.stub.OpinionSubjectTestDataFactory.cappuccino1A
import com.r8n.backend.mock.stub.OpinionSubjectTestDataFactory.cappuccino1G
import com.r8n.backend.opinions.api.dto.OpinionDto
import com.r8n.backend.opinions.api.dto.OpinionStatusEnumDto
import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.Instant
import java.util.UUID

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Import(TestObjectMapperConfiguration::class)
class OpinionsIntegrationTests {
    private companion object {
        val CURRENT_USER_ID = bernardReferent.id

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
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var usersInternalApi: UsersInternalApi

    @Autowired
    lateinit var serviceTokenService: ServiceTokenService

    @BeforeEach
    fun setUp() {
        whenever(usersInternalApi.getUserName(eq(bernardReferent.id)))
            .thenReturn(bernardReferent.name)
        whenever(usersInternalApi.getUserName(eq(CURRENT_USER_ID)))
            .thenReturn(bernardReferent.name)
    }

    @Test
    @WithMockUser
    fun `get opinion works`() {
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))
        val requestedId = "30000000-0000-0000-0000-000000000001"
        val result =
            mockMvc
                .perform(
                    get("/api/opinions/$requestedId")
                        .with(csrf())
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val actual: OpinionDto = objectMapper.readValue(result.response.contentAsString)
        val expected =
            OpinionDto(
                UUID.fromString("30000000-0000-0000-0000-000000000001"),
                bernardReferent.id,
                bernardReferent.name,
                cappuccino1A.id,
                cappuccino1A.name,
                listOf("reminds of grandma's home coffee"),
                listOf("5.50€", "lactose-free milk"),
                4.23,
                null,
                emptyList(),
                OpinionStatusEnumDto.DRAFT,
                Instant.parse("2024-02-01T09:30:00Z"),
            )

        assertEquals(expected, actual)
    }

    @Test
    @WithMockUser
    fun `get opinion for subject works`() {
        val requestedSubjectId = "14141414-1414-1414-1414-141414141414"
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))
        val result =
            mockMvc
                .perform(
                    get("/api/opinions/for/$requestedSubjectId")
                        .with(csrf())
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val actual: OpinionDto = objectMapper.readValue(result.response.contentAsString)
        val expected =
            OpinionDto(
                UUID.fromString("30000000-0000-0000-0000-000000000001"),
                bernardReferent.id,
                bernardReferent.name,
                cappuccino1A.id,
                cappuccino1A.name,
                listOf("reminds of grandma's home coffee"),
                listOf("5.50€", "lactose-free milk"),
                4.23,
                null,
                emptyList(),
                OpinionStatusEnumDto.DRAFT,
                Instant.parse("2024-02-01T09:30:00Z"),
            )
        assertEquals(expected, actual)
    }

    @Test
    @WithMockUser
    fun `create opinion works`() {
        val subjectId = "15151515-1515-1515-1515-151515151515"
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))
        val result =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", subjectId)
                        .queryParam("subjective", "new subjective")
                        .queryParam("objective", "new objective")
                        .queryParam("mark", "4.50")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val actual: OpinionDto = objectMapper.readValue(result.response.contentAsString)
        assertEquals(CURRENT_USER_ID, actual.owner)
        assertEquals(bernardReferent.name, actual.ownerName)
        assertEquals(UUID.fromString(subjectId), actual.subject)
        assertEquals(cappuccino1G.name, actual.subjectName)
        assertEquals(listOf("new subjective"), actual.subjective)
        assertEquals(listOf("new objective"), actual.objective)
        assertEquals(4.5, actual.mark)
        assertEquals(OpinionStatusEnumDto.DRAFT, actual.status)
    }

    @Test
    @WithMockUser
    fun `update opinion works`() {
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))
        val createResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "to be replaced subjective")
                        .queryParam("objective", "to be replaced objective")
                        .queryParam("mark", "2.10")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val created: OpinionDto = objectMapper.readValue(createResult.response.contentAsString)

        val updateResult =
            mockMvc
                .perform(
                    patch("/api/opinions/${created.id}")
                        .with(csrf())
                        .queryParam("subjective", "updated subjective")
                        .queryParam("objective", "updated objective")
                        .queryParam("mark", "4.90")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val actual: OpinionDto = objectMapper.readValue(updateResult.response.contentAsString)
        assertEquals(created.id, actual.id)
        assertEquals(CURRENT_USER_ID, actual.owner)
        assertEquals(bernardReferent.name, actual.ownerName)
        assertEquals(UUID.fromString("15151515-1515-1515-1515-151515151515"), actual.subject)
        assertEquals(cappuccino1G.name, actual.subjectName)
        assertEquals(listOf("updated subjective"), actual.subjective)
        assertEquals(listOf("updated objective"), actual.objective)
        assertEquals(4.9, actual.mark)
        assertEquals(OpinionStatusEnumDto.DRAFT, actual.status)
    }

    @Test
    @WithMockUser
    fun `delete opinion works`() {
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))
        val createResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "to be removed subjective")
                        .queryParam("objective", "to be removed objective")
                        .queryParam("mark", "1.10")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val created: OpinionDto = objectMapper.readValue(createResult.response.contentAsString)

        mockMvc
            .perform(
                delete("/api/opinions/${created.id}")
                    .with(csrf())
                    .header("Authorization", "Bearer $accessToken"),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                get("/api/opinions/${created.id}")
                    .header("Authorization", "Bearer $accessToken"),
            ).andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `link component works`() {
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))

        val parentCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "parent subjective")
                        .queryParam("objective", "parent objective")
                        .queryParam("mark", "2.00")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val parent: OpinionDto = objectMapper.readValue(parentCreateResult.response.contentAsString)

        val childCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "child subjective")
                        .queryParam("objective", "child objective")
                        .queryParam("mark", "4.00")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val child: OpinionDto = objectMapper.readValue(childCreateResult.response.contentAsString)

        val linkResult =
            mockMvc
                .perform(
                    post("/api/opinions/link")
                        .with(csrf())
                        .queryParam("parentOpinionId", parent.id.toString())
                        .queryParam("childOpinionId", child.id.toString())
                        .queryParam("weight", "0.25")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val actual: OpinionDto = objectMapper.readValue(linkResult.response.contentAsString)
        assertEquals(parent.id, actual.id)
        assertEquals(1, actual.components.size)
        assertEquals(child.id, actual.components.first().opinion)
        assertEquals(0.25, actual.components.first().weight)
        assertEquals(1.0, actual.componentMark)
    }

    @Test
    @WithMockUser
    fun `link component is idempotent for duplicate request`() {
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))

        val parentCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "parent duplicate subjective")
                        .queryParam("objective", "parent duplicate objective")
                        .queryParam("mark", "2.00")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val parent: OpinionDto = objectMapper.readValue(parentCreateResult.response.contentAsString)

        val childCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "child duplicate subjective")
                        .queryParam("objective", "child duplicate objective")
                        .queryParam("mark", "4.00")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val child: OpinionDto = objectMapper.readValue(childCreateResult.response.contentAsString)

        mockMvc
            .perform(
                post("/api/opinions/link")
                    .with(csrf())
                    .queryParam("parentOpinionId", parent.id.toString())
                    .queryParam("childOpinionId", child.id.toString())
                    .queryParam("weight", "0.25")
                    .header("Authorization", "Bearer $accessToken"),
            ).andExpect(status().isOk)

        val secondLinkResult =
            mockMvc
                .perform(
                    post("/api/opinions/link")
                        .with(csrf())
                        .queryParam("parentOpinionId", parent.id.toString())
                        .queryParam("childOpinionId", child.id.toString())
                        .queryParam("weight", "0.25")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val actual: OpinionDto = objectMapper.readValue(secondLinkResult.response.contentAsString)
        assertEquals(1, actual.components.size)
        assertEquals(child.id, actual.components.first().opinion)
        assertEquals(0.25, actual.components.first().weight)
        assertEquals(1.0, actual.componentMark)
    }

    @Test
    @WithMockUser
    fun `link component forbidden for non owner`() {
        val ownerToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))
        val otherUserToken = serviceTokenService.generateAccessToken(UUID.randomUUID(), listOf("USER"))

        val parentCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "parent owner subjective")
                        .queryParam("objective", "parent owner objective")
                        .queryParam("mark", "2.00")
                        .header("Authorization", "Bearer $ownerToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val parent: OpinionDto = objectMapper.readValue(parentCreateResult.response.contentAsString)

        mockMvc
            .perform(
                post("/api/opinions/link")
                    .with(csrf())
                    .queryParam("parentOpinionId", parent.id.toString())
                    .queryParam("childOpinionId", "30000000-0000-0000-0000-000000000001")
                    .queryParam("weight", "0.25")
                    .header("Authorization", "Bearer $otherUserToken"),
            ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser
    fun `unlink component works`() {
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))

        val parentCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "parent unlink subjective")
                        .queryParam("objective", "parent unlink objective")
                        .queryParam("mark", "2.00")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val parent: OpinionDto = objectMapper.readValue(parentCreateResult.response.contentAsString)

        val childCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "child unlink subjective")
                        .queryParam("objective", "child unlink objective")
                        .queryParam("mark", "4.00")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val child: OpinionDto = objectMapper.readValue(childCreateResult.response.contentAsString)

        val linkResult =
            mockMvc
                .perform(
                    post("/api/opinions/link")
                        .with(csrf())
                        .queryParam("parentOpinionId", parent.id.toString())
                        .queryParam("childOpinionId", child.id.toString())
                        .queryParam("weight", "0.25")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val linked: OpinionDto = objectMapper.readValue(linkResult.response.contentAsString)
        val linkId = linked.components.first().id

        val unlinkResult =
            mockMvc
                .perform(
                    delete("/api/opinions/unlink/$linkId")
                        .with(csrf())
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val actual: OpinionDto = objectMapper.readValue(unlinkResult.response.contentAsString)

        assertEquals(parent.id, actual.id)
        assertEquals(0, actual.components.size)
        assertEquals(null, actual.componentMark)
    }

    @Test
    @WithMockUser
    fun `unlink component forbidden for non owner`() {
        val ownerToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))
        val otherUserToken = serviceTokenService.generateAccessToken(UUID.randomUUID(), listOf("USER"))

        val parentCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "parent unlink owner subjective")
                        .queryParam("objective", "parent unlink owner objective")
                        .queryParam("mark", "2.00")
                        .header("Authorization", "Bearer $ownerToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val parent: OpinionDto = objectMapper.readValue(parentCreateResult.response.contentAsString)

        val linkResult =
            mockMvc
                .perform(
                    post("/api/opinions/link")
                        .with(csrf())
                        .queryParam("parentOpinionId", parent.id.toString())
                        .queryParam("childOpinionId", "30000000-0000-0000-0000-000000000001")
                        .queryParam("weight", "0.25")
                        .header("Authorization", "Bearer $ownerToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val linked: OpinionDto = objectMapper.readValue(linkResult.response.contentAsString)
        val linkId = linked.components.first().id

        mockMvc
            .perform(
                delete("/api/opinions/unlink/$linkId")
                    .with(csrf())
                    .header("Authorization", "Bearer $otherUserToken"),
            ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser
    fun `adjust component weight works`() {
        val accessToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))

        val parentCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "parent adjust subjective")
                        .queryParam("objective", "parent adjust objective")
                        .queryParam("mark", "2.00")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val parent: OpinionDto = objectMapper.readValue(parentCreateResult.response.contentAsString)

        val childCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "child adjust subjective")
                        .queryParam("objective", "child adjust objective")
                        .queryParam("mark", "4.00")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val child: OpinionDto = objectMapper.readValue(childCreateResult.response.contentAsString)

        val linkResult =
            mockMvc
                .perform(
                    post("/api/opinions/link")
                        .with(csrf())
                        .queryParam("parentOpinionId", parent.id.toString())
                        .queryParam("childOpinionId", child.id.toString())
                        .queryParam("weight", "0.25")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val linked: OpinionDto = objectMapper.readValue(linkResult.response.contentAsString)
        val linkId = linked.components.first().id

        val adjustResult =
            mockMvc
                .perform(
                    patch("/api/opinions/adjust-weight/$linkId")
                        .with(csrf())
                        .queryParam("weight", "0.50")
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val actual: OpinionDto = objectMapper.readValue(adjustResult.response.contentAsString)

        assertEquals(parent.id, actual.id)
        assertEquals(1, actual.components.size)
        assertEquals(linkId, actual.components.first().id)
        assertEquals(child.id, actual.components.first().opinion)
        assertEquals(0.5, actual.components.first().weight)
        assertEquals(2.0, actual.componentMark)
    }

    @Test
    @WithMockUser
    fun `adjust component weight forbidden for non owner`() {
        val ownerToken = serviceTokenService.generateAccessToken(CURRENT_USER_ID, listOf("USER"))
        val otherUserToken = serviceTokenService.generateAccessToken(UUID.randomUUID(), listOf("USER"))

        val parentCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "parent adjust owner subjective")
                        .queryParam("objective", "parent adjust owner objective")
                        .queryParam("mark", "2.00")
                        .header("Authorization", "Bearer $ownerToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val parent: OpinionDto = objectMapper.readValue(parentCreateResult.response.contentAsString)

        val childCreateResult =
            mockMvc
                .perform(
                    post("/api/opinions")
                        .with(csrf())
                        .queryParam("subjectId", "15151515-1515-1515-1515-151515151515")
                        .queryParam("subjective", "child adjust owner subjective")
                        .queryParam("objective", "child adjust owner objective")
                        .queryParam("mark", "4.00")
                        .header("Authorization", "Bearer $ownerToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val child: OpinionDto = objectMapper.readValue(childCreateResult.response.contentAsString)

        val linkResult =
            mockMvc
                .perform(
                    post("/api/opinions/link")
                        .with(csrf())
                        .queryParam("parentOpinionId", parent.id.toString())
                        .queryParam("childOpinionId", child.id.toString())
                        .queryParam("weight", "0.25")
                        .header("Authorization", "Bearer $ownerToken"),
                ).andExpect(status().isOk)
                .andReturn()
        val linked: OpinionDto = objectMapper.readValue(linkResult.response.contentAsString)
        val linkId = linked.components.first().id

        mockMvc
            .perform(
                patch("/api/opinions/adjust-weight/$linkId")
                    .with(csrf())
                    .queryParam("weight", "0.50")
                    .header("Authorization", "Bearer $otherUserToken"),
            ).andExpect(status().isForbidden)
    }
}