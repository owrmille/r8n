package com.r8n.backend.messaging

import com.r8n.backend.messaging.api.MessagingApi.Companion.SUPPORT_THREAD_PATH
import com.r8n.backend.messaging.api.MessagingApi.Companion.SUPPORT_THREADS_PATH
import com.r8n.backend.messaging.api.dto.messaging.SUPPORT_MESSAGE_TEXT_MAX_LENGTH
import com.r8n.backend.messaging.provider.database.SupportMessageRepository
import com.r8n.backend.messaging.provider.database.SupportThreadRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
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
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SupportMessagingIntegrationTests {
    private companion object {
        @Suppress("unused") // used to store test database container
        @Container
        @ServiceConnection
        val postgres: PostgreSQLContainer =
            PostgreSQLContainer(DockerImageName.parse("postgres:15"))
                .withDatabaseName("messaging")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("db/init-schema.sql")

        val userAId: UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val userBId: UUID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        val supportId: UUID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
        val adminId: UUID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
        val moderatorId: UUID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
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
        val threadId = createThread(userAId, "USER", "Need help with a review dispute")

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(userAId.toString()).roles("USER")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].threadId").value(threadId.toString()))
            .andExpect(jsonPath("$.items[0].authorUserId").value(userAId.toString()))
            .andExpect(jsonPath("$.items[0].authorRole").value("USER"))
    }

    @Test
    fun `user cannot read another user thread`() {
        val threadId = createThread(userAId, "USER", "My private support thread")

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(userBId.toString()).roles("USER")),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `support can read another user thread`() {
        val threadId = createThread(userAId, "USER", "Need a support response")
        addMessage(supportId, "SUPPORT", threadId, "Support response")

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(supportId.toString()).roles("SUPPORT")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[1].authorRole").value("SUPPORT"))
    }

    @Test
    fun `support creates own support thread as requester`() {
        val threadId = createThread(supportId, "SUPPORT", "Support user needs private support")

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(supportId.toString()).roles("SUPPORT")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].id").value(threadId.toString()))
            .andExpect(jsonPath("$.items[0].viewerRole").value("REQUESTER"))

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(supportId.toString()).roles("SUPPORT")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].authorRole").value("USER"))
    }

    @Test
    fun `support writes to own support thread as requester`() {
        val threadId = createThread(supportId, "SUPPORT", "Support user needs private support")

        mockMvc
            .perform(
                post(messagesPath(threadId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"text":"Requester follow-up"}""")
                    .with(user(supportId.toString()).roles("SUPPORT"))
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.authorUserId").value(supportId.toString()))
            .andExpect(jsonPath("$.authorRole").value("USER"))
    }

    @Test
    fun `admin can read and respond to another user thread as support`() {
        val threadId = createThread(userAId, "USER", "Need admin support")

        mockMvc
            .perform(
                post(messagesPath(threadId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"text":"Admin support response"}""")
                    .with(user(adminId.toString()).roles("ADMIN"))
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.authorUserId").value(adminId.toString()))
            .andExpect(jsonPath("$.authorRole").value("SUPPORT"))

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(adminId.toString()).roles("ADMIN")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[1].authorRole").value("SUPPORT"))
    }

    @Test
    fun `admin creates own support thread as requester`() {
        val threadId = createThread(adminId, "ADMIN", "Admin needs private support")

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(adminId.toString()).roles("ADMIN")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].id").value(threadId.toString()))
            .andExpect(jsonPath("$.items[0].viewerRole").value("REQUESTER"))

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(adminId.toString()).roles("ADMIN")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].authorRole").value("USER"))
    }

    @Test
    fun `moderator can use own support threads as regular user`() {
        val threadId = createThread(moderatorId, "MODERATOR", "Moderator needs user-side support")

        mockMvc
            .perform(
                post(messagesPath(threadId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"text":"Moderator follow-up"}""")
                    .with(user(moderatorId.toString()).roles("MODERATOR"))
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.authorUserId").value(moderatorId.toString()))
            .andExpect(jsonPath("$.authorRole").value("USER"))

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(moderatorId.toString()).roles("MODERATOR")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[1].authorRole").value("USER"))
    }

    @Test
    fun `moderator cannot access another user support thread`() {
        val threadId = createThread(userAId, "USER", "Private support thread")

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(moderatorId.toString()).roles("MODERATOR")),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `posting blank message returns bad request`() {
        val threadId = createThread(userAId, "USER", "Need clarification")

        mockMvc
            .perform(
                post(messagesPath(threadId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"text":"   "}""")
                    .with(user(userAId.toString()).roles("USER"))
                    .with(csrf()),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `creating thread with oversized initial message returns bad request`() {
        val oversizedMessage = "a".repeat(SUPPORT_MESSAGE_TEXT_MAX_LENGTH + 1)

        mockMvc
            .perform(
                post(SUPPORT_THREADS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("initialMessage" to oversizedMessage)))
                    .with(user(userAId.toString()).roles("USER"))
                    .with(csrf()),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `posting oversized message returns bad request`() {
        val threadId = createThread(userAId, "USER", "Need clarification")
        val oversizedMessage = "a".repeat(SUPPORT_MESSAGE_TEXT_MAX_LENGTH + 1)

        mockMvc
            .perform(
                post(messagesPath(threadId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("text" to oversizedMessage)))
                    .with(user(userAId.toString()).roles("USER"))
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
                    .with(user(supportId.toString()).roles("SUPPORT")),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `support sees all threads in list while user sees only own`() {
        createThread(userAId, "USER", "Thread from user A")
        createThread(userBId, "USER", "Thread from user B")

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(userAId.toString()).roles("USER")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].viewerRole").value("REQUESTER"))

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(supportId.toString()).roles("SUPPORT")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].viewerRole").value("SUPPORT"))
            .andExpect(jsonPath("$.items[1].viewerRole").value("SUPPORT"))
    }

    @Test
    fun `admin sees all support threads in list`() {
        createThread(userAId, "USER", "Thread from user A")
        createThread(userBId, "USER", "Thread from user B")

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(adminId.toString()).roles("ADMIN")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
    }

    @Test
    fun `thread summaries respect requested page size`() {
        createThread(userAId, "USER", "Thread one")
        createThread(userAId, "USER", "Thread two")

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "1")
                    .with(user(supportId.toString()).roles("SUPPORT")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.size").value(1))
    }

    @Test
    fun `thread summaries are ordered by latest message date`() {
        val olderThreadId = createThread(userAId, "USER", "Thread one")
        createThread(userAId, "USER", "Thread two")
        addMessage(userAId, "USER", olderThreadId, "Follow-up makes this thread latest")

        mockMvc
            .perform(
                get(SUPPORT_THREADS_PATH)
                    .param("page", "0")
                    .param("size", "20")
                    .with(user(supportId.toString()).roles("SUPPORT")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].id").value(olderThreadId.toString()))
            .andExpect(jsonPath("$.items[0].lastMessageText").value("Follow-up makes this thread latest"))
    }

    @Test
    fun `thread messages respect requested page size`() {
        val threadId = createThread(userAId, "USER", "Message one")
        addMessage(userAId, "USER", threadId, "Message two")

        mockMvc
            .perform(
                get(messagesPath(threadId))
                    .param("page", "0")
                    .param("size", "1")
                    .with(user(userAId.toString()).roles("USER")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.size").value(1))
    }

    @Test
    fun `owner can delete their thread and its messages`() {
        val threadId = createThread(userAId, "USER", "Please help")
        addMessage(userAId, "USER", threadId, "More context")

        mockMvc
            .perform(
                delete(threadPath(threadId))
                    .with(user(userAId.toString()).roles("USER"))
                    .with(csrf()),
            ).andExpect(status().isNoContent)

        assert(!supportThreadRepository.existsById(threadId))
        assert(supportMessageRepository.findAllByThreadIdOrderByCreatedAtAsc(threadId, org.springframework.data.domain.Pageable.unpaged()).isEmpty)
    }

    @Test
    fun `support can delete any thread`() {
        val threadId = createThread(userAId, "USER", "Please help")

        mockMvc
            .perform(
                delete(threadPath(threadId))
                    .with(user(supportId.toString()).roles("SUPPORT"))
                    .with(csrf()),
            ).andExpect(status().isNoContent)

        assert(!supportThreadRepository.existsById(threadId))
    }

    @Test
    fun `user cannot delete another user's thread`() {
        val threadId = createThread(userAId, "USER", "Please help")

        mockMvc
            .perform(
                delete(threadPath(threadId))
                    .with(user(userBId.toString()).roles("USER"))
                    .with(csrf()),
            ).andExpect(status().isForbidden)

        assert(supportThreadRepository.existsById(threadId))
    }

    @Test
    fun `deleting non-existent thread returns 404`() {
        mockMvc
            .perform(
                delete(threadPath(UUID.randomUUID()))
                    .with(user(userAId.toString()).roles("USER"))
                    .with(csrf()),
            ).andExpect(status().isNotFound)
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
        return UUID.fromString(body["id"].asString())
    }

    private fun addMessage(
        authorId: UUID,
        role: String,
        threadId: UUID,
        text: String,
    ) {
        mockMvc
            .perform(
                post(messagesPath(threadId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"text":"$text"}""")
                    .with(user(authorId.toString()).roles(role))
                    .with(csrf()),
            ).andExpect(status().isOk)
    }

    private fun threadPath(threadId: UUID): String = SUPPORT_THREAD_PATH.replace("{threadId}", threadId.toString())

    private fun messagesPath(threadId: UUID): String = "/api/messaging/support/threads/$threadId/messages"
}
