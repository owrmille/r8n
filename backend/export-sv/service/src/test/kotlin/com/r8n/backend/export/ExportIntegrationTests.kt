package com.r8n.backend.export

import com.r8n.backend.core.utils.toResponse
import com.r8n.backend.export.api.ExportApi
import com.r8n.backend.export.api.dto.ExportStateDto
import com.r8n.backend.export.api.dto.ExportStatus
import com.r8n.backend.export.api.dto.UserCompleteDataDto
import com.r8n.backend.mock.api.IncomingAccessRequestApi
import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.OutgoingAccessRequestApi
import com.r8n.backend.mock.integration.api.OpinionListInternalApi
import com.r8n.backend.mock.stub.AccessRequestsTestDataFactory
import com.r8n.backend.mock.stub.MiscTestFactory
import com.r8n.backend.mock.stub.OpinionListTestDataFactory
import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.api.dto.ConsentDto
import com.r8n.backend.users.api.dto.PersonalIdentifiableInformationSectionDto
import com.r8n.backend.users.api.dto.UserDto
import com.r8n.backend.users.api.dto.UserSessionDto
import com.r8n.backend.users.api.dto.UserStatusEnumDto
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@ActiveProfiles("test")
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestObjectMapperConfiguration::class)
class ExportIntegrationTests {
    private companion object {
        const val USER_ID = "00000000-0000-0000-0000-000000000000"
        val opinions = OpinionListTestDataFactory.getList()
        val incomingAccessRequests = AccessRequestsTestDataFactory.get()
        val outgoingAccessRequests = AccessRequestsTestDataFactory.get()
        val supportMessages = MiscTestFactory.getSupportMessage()
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var serviceTokenService: ServiceTokenService

    @MockitoBean
    lateinit var opinionClient: OpinionListInternalApi

    @MockitoBean
    lateinit var incomingAccessRequestClient: IncomingAccessRequestApi

    @MockitoBean
    lateinit var outgoingAccessRequestClient: OutgoingAccessRequestApi

    @MockitoBean
    lateinit var messageClient: MessagingApi

    @MockitoBean
    lateinit var usersInternalApi: UsersInternalApi

    @BeforeEach
    fun setUp() {
        val timestamp = LocalDateTime.of(2024, 1, 1, 12, 0).toInstant(ZoneOffset.UTC)
        val session =
            UserSessionDto(
                UUID.fromString("01010101-0101-0101-0101-010101010101"),
                timestamp,
                timestamp.plus(1, ChronoUnit.DAYS),
                "127.0.0.1",
                "Test User Agent",
            )
        val userDto =
            UserDto(
                UUID.fromString(USER_ID),
                "Test Testsson",
                "test@test.test",
                UserStatusEnumDto.ACTIVE,
                timestamp,
                listOf(ConsentDto("PRIVACY_POLICY", timestamp, session)),
            )

        whenever(usersInternalApi.getUser(any())).thenReturn(userDto)
        whenever(usersInternalApi.getSessionsForUser(any(), anyOrNull())).thenReturn(
            PageImpl(listOf(session)).toResponse(),
        )
        whenever(opinionClient.getMineFull(any())).thenReturn(
            PageImpl(listOf(opinions)).toResponse(),
        )
        whenever(incomingAccessRequestClient.get(anyOrNull(), anyOrNull(), anyOrNull(), any())).thenReturn(
            PageImpl(listOf(incomingAccessRequests)).toResponse(),
        )
        whenever(outgoingAccessRequestClient.get(anyOrNull(), anyOrNull(), anyOrNull(), any())).thenReturn(
            PageImpl(listOf(outgoingAccessRequests)).toResponse(),
        )
        whenever(messageClient.getSupportThreads()).thenReturn(
            PageImpl(listOf(supportMessages)).toResponse(),
        )
    }

    @Test
    @WithMockUser(username = USER_ID)
    fun `export process follows three steps`() {
        val accessToken = serviceTokenService.generateAccessToken(UUID.fromString(USER_ID), listOf("USER"))

        // 1. Request export creation
        mockMvc
            .perform(
                post(ExportApi.START_PATH)
                    .header("Authorization", "Bearer $accessToken"),
            ).andExpect(status().isAccepted)

        // 2. Check status
        val statusResult =
            mockMvc
                .perform(
                    get(ExportApi.STATUS_PATH)
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val exportState: ExportStateDto = objectMapper.readValue(statusResult.response.contentAsString)
        assertEquals(ExportStatus.COMPLETED, exportState.status)

        // 3. Download data
        val downloadResult =
            mockMvc
                .perform(
                    get(ExportApi.DOWNLOAD_PATH)
                        .header("Authorization", "Bearer $accessToken"),
                ).andExpect(status().isOk)
                .andReturn()

        val actual: UserCompleteDataDto = objectMapper.readValue(downloadResult.response.contentAsString)

        val timestamp = LocalDateTime.of(2024, 1, 1, 12, 0).toInstant(ZoneOffset.UTC)
        val session =
            UserSessionDto(
                UUID.fromString("01010101-0101-0101-0101-010101010101"),
                timestamp,
                timestamp.plus(
                    1,
                    ChronoUnit.DAYS,
                ),
                "127.0.0.1",
                "Test User Agent",
            )

        val expected =
            UserCompleteDataDto(
                UUID.fromString(USER_ID),
                UserStatusEnumDto.ACTIVE,
                timestamp,
                PageImpl(
                    listOf(
                        ConsentDto("PRIVACY_POLICY", timestamp, session),
                    ),
                ).toResponse(),
                PersonalIdentifiableInformationSectionDto(
                    "Test Testsson",
                    "test@test.test",
                    sessions = PageImpl(listOf(session)).toResponse(),
                ),
                PageImpl(listOf(opinions)).toResponse(),
                PageImpl(listOf(outgoingAccessRequests)).toResponse(),
                PageImpl(listOf(incomingAccessRequests)).toResponse(),
                PageImpl(listOf(supportMessages)).toResponse(),
            )
        assertEquals(expected, actual)
    }
}