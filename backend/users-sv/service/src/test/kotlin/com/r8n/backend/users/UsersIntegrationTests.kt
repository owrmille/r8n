package com.r8n.backend.users

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.mock.api.IncomingAccessRequestApi
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.OutgoingAccessRequestApi
import com.r8n.backend.mock.integration.api.OpinionListInternalApi
import com.r8n.backend.mock.stub.AccessRequestsTestDataFactory
import com.r8n.backend.mock.stub.MiscTestFactory
import com.r8n.backend.mock.stub.OpinionListTestDataFactory
import com.r8n.backend.users.api.dto.UserCompleteDataDto
import com.r8n.backend.users.domain.UserStatusEnum
import com.r8n.backend.users.persistence.ConsentPersistence
import com.r8n.backend.users.persistence.PIIPersistence
import com.r8n.backend.users.persistence.UserPersistence
import com.r8n.backend.users.persistence.UserSessionPersistence
import com.r8n.backend.users.provider.database.ConsentRepository
import com.r8n.backend.users.provider.database.PIIRepository
import com.r8n.backend.users.provider.database.UserRepository
import com.r8n.backend.users.provider.database.UserSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Instant
import java.util.UUID

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestObjectMapperConfiguration::class)
class UsersIntegrationTests {

    private companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15"))
            .withDatabaseName("users")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/init-schema.sql")
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var piiRepository: PIIRepository

    @Autowired
    lateinit var sessionRepository: UserSessionRepository

    @Autowired
    lateinit var consentRepository: ConsentRepository

    @MockitoBean
    lateinit var opinionClient: OpinionListInternalApi

    @MockitoBean
    lateinit var incomingAccessRequestClient: IncomingAccessRequestApi

    @MockitoBean
    lateinit var outgoingAccessRequestClient: OutgoingAccessRequestApi

    @MockitoBean
    lateinit var messageClient: MessagingApi

    private val userId = UUID.fromString("07070707-0707-0707-0707-070707070707")

    @BeforeEach
    fun setUp() {
        consentRepository.deleteAllInBatch()
        sessionRepository.deleteAllInBatch()
        piiRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()

        val user = UserPersistence(userId, UserStatusEnum.ACTIVE, Instant.now())
        userRepository.saveAndFlush(user)

        val pii = PIIPersistence(userId, "Test User", "test@example.com", "")
        piiRepository.saveAndFlush(pii)

        val session = UserSessionPersistence(UUID.randomUUID(), userId, Instant.now(), Instant.now().plusSeconds(3600), "127.0.0.1", "Mock Browser")
        sessionRepository.saveAndFlush(session)

        val consent = ConsentPersistence(UUID.randomUUID(), userId, "COOKIES", Instant.now(), session.id)
        consentRepository.saveAndFlush(consent)

        whenever(opinionClient.getMineFull(any())).thenReturn(
            PageImpl(listOf(OpinionListTestDataFactory.getList(userId))).toResponse()
        )
        whenever(incomingAccessRequestClient.get(any(), any(), any(), any())).thenReturn(
            PageImpl(listOf(AccessRequestsTestDataFactory.get())).toResponse()
        )
        whenever(outgoingAccessRequestClient.get(any(), any(), any(), any())).thenReturn(
            PageImpl(listOf(AccessRequestsTestDataFactory.get())).toResponse()
        )
        whenever(messageClient.getSupportThreads()).thenReturn(
            PageImpl(listOf(MiscTestFactory.getSupportMessage())).toResponse()
        )
    }

    @Test
    @WithMockUser(username = "07070707-0707-0707-0707-070707070707")
    fun `exportAll returns complete user data`() {
        val result = mockMvc.perform(
            get("/users/export")
        )
            .andExpect(status().isOk)
            .andReturn()

        val actual: UserCompleteDataDto = objectMapper.readValue(result.response.contentAsString)

        assertEquals(userId, actual.id)
        assertEquals("Test User", actual.personalIdentifiableInformation.name)
        assertEquals("test@example.com", actual.personalIdentifiableInformation.email)
        assertEquals(1, actual.personalIdentifiableInformation.sessions.items.size)
        assertEquals(1, actual.consents.items.size)
        assertEquals(1, actual.opinions.items.size)
        assertEquals(1, actual.incomingRequests.items.size)
        assertEquals(1, actual.outgoingRequests.items.size)
        assertEquals(1, actual.messages.items.size)
    }
}
