package com.r8n.backend.mock

import com.r8n.backend.mock.api.MessagingApi.Companion.SUPPORT_THREADS_PATH
import com.r8n.backend.mock.provider.database.SupportMessageRepository
import com.r8n.backend.mock.provider.database.SupportThreadRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.autoconfigure.exclude=",
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=db/changelog/mock/db.changelog-master.sql",
        "spring.liquibase.default-schema=mock",
        "spring.liquibase.contexts=test",
    ],
)
class SupportMessagingIntegrationTests {
    private companion object {
        @Container
        @ServiceConnection
        val postgres: PostgreSQLContainer =
            PostgreSQLContainer(DockerImageName.parse("postgres:15"))
                .withDatabaseName("mock")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("db/init-schema.sql")

        val USER_A_ID: UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val USER_B_ID: UUID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        val SUPPORT_ID: UUID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var supportThreadRepository: SupportThreadRepository

    @Autowired
    lateinit var supportMessageRepository: SupportMessageRepository

    @BeforeEach
    fun setUp() {
        supportMessageRepository.deleteAll()
        supportThreadRepository.deleteAll()
    }

    @Test
    fun `user can create thread and read its messages`() {
        val threadId = createThread(USER_A_ID, "USER", "Need help with a review dispute")

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(USER_A_ID.toString()).roles("USER")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].threadId").value(threadId.toString()))
            .andExpect(jsonPath("$.items[0].authorUserId").value(USER_A_ID.toString()))
            .andExpect(jsonPath("$.items[0].authorRole").value("USER"))
    }

    @Test
    fun `user cannot read another user thread`() {
        val threadId = createThread(USER_A_ID, "USER", "My private support thread")

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(USER_B_ID.toString()).roles("USER")),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `support can read another user thread`() {
        val threadId = createThread(USER_A_ID, "USER", "Need a support response")

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(SUPPORT_ID.toString()).roles("SUPPORT")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
    }

    @Test
    fun `posting blank message returns bad request`() {
        val threadId = createThread(USER_A_ID, "USER", "Need clarification")

        mockMvc
            .perform(
                post(messagesPath(threadId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"text":"   "}""")
                    .with(user(USER_A_ID.toString()).roles("USER"))
                    .with(csrf()),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `missing thread returns not found`() {
        mockMvc
            .perform(
                get(messagesPath(UUID.randomUUID()))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(SUPPORT_ID.toString()).roles("SUPPORT")),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `support sees all threads in list while user sees only own`() {
        createThread(USER_A_ID, "USER", "Thread from user A")
        createThread(USER_B_ID, "USER", "Thread from user B")

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(USER_A_ID.toString()).roles("USER")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(SUPPORT_ID.toString()).roles("SUPPORT")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
    }

    private fun createThread(
        authorId: UUID,
        role: String,
        initialMessage: String,
    ): UUID {
        val result =
            mockMvc
                .perform(
                    post(SUPPORT_THREADS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"initialMessage":"$initialMessage"}""")
                        .with(user(authorId.toString()).roles(role))
                        .with(csrf()),
                ).andExpect(status().isOk)
                .andReturn()

        val body = objectMapper.readTree(result.response.contentAsString)
        return UUID.fromString(body["id"].asText())
    }

    private fun messagesPath(threadId: UUID): String = "/api/messaging/support/threads/$threadId/messages"
}