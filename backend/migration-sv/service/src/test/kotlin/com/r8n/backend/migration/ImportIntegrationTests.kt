package com.r8n.backend.migration

import com.r8n.backend.core.utils.TestObjectMapperConfiguration
import com.r8n.backend.messaging.api.MessagingApi
import com.r8n.backend.opinions.api.access.IncomingAccessRequestApi
import com.r8n.backend.opinions.api.access.OutgoingAccessRequestApi
import com.r8n.backend.opinions.api.lists.OpinionListsApi
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.api.lists.dto.OpinionListPrivacyEnumDto
import com.r8n.backend.opinions.api.opinions.OpinionsApi
import com.r8n.backend.opinions.api.opinions.dto.OpinionDto
import com.r8n.backend.opinions.api.opinions.dto.OpinionStatusEnumDto
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID

@ActiveProfiles("test")
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestObjectMapperConfiguration::class)
class ImportIntegrationTests {
    private companion object {
        const val USER_A_ID = "10000000-0000-0000-0000-000000000000"
        const val USER_B_ID = "20000000-0000-0000-0000-000000000000"
        val LIST_A_ID = UUID.fromString("11000000-0000-0000-0000-000000000000")
        val LIST_B_ID = UUID.fromString("21000000-0000-0000-0000-000000000000")
        val OPINION_A1_ID = UUID.fromString("1a000000-0000-0000-0000-000000000001")
        val OPINION_A2_ID = UUID.fromString("1a000000-0000-0000-0000-000000000002")
        val OPINION_B1_ID = UUID.fromString("2b000000-0000-0000-0000-000000000001")
        val SUBJECT_1_ID = UUID.fromString("51000000-0000-0000-0000-000000000000")
        val SUBJECT_2_ID = UUID.fromString("52000000-0000-0000-0000-000000000000")
        val SUBJECT_3_ID = UUID.fromString("53000000-0000-0000-0000-000000000000")
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var serviceTokenService: ServiceTokenService

    @MockitoBean
    lateinit var usersInternalApi: UsersInternalApi

    @MockitoBean
    lateinit var opinionsApi: OpinionsApi

    @MockitoBean
    lateinit var opinionListsApi: OpinionListsApi

    @MockitoBean
    lateinit var outgoingAccessRequestApi: OutgoingAccessRequestApi

    @MockitoBean
    lateinit var incomingAccessRequestApi: IncomingAccessRequestApi

    @MockitoBean
    lateinit var opinionListsInternalApi: OpinionListsInternalApi

    @MockitoBean
    lateinit var messagingApi: MessagingApi

    @BeforeEach
    fun setUp() {
        whenever(usersInternalApi.getUserName(any())).thenReturn("Some User")

        // Mock successful opinion creation with any arguments to avoid matching issues
        whenever(opinionsApi.createOpinion(any(), any(), any(), anyOrNull())).thenAnswer {
            val subjectId = it.getArgument<UUID>(0)
            createOpinionDto(UUID.randomUUID(), USER_A_ID, subjectId)
        }

        // Mock list creation
        whenever(opinionListsApi.createList(any(), any())).thenAnswer {
            createOpinionListDto(UUID.randomUUID(), USER_A_ID, it.getArgument(0))
        }
    }

    @Test
    @WithMockUser(username = USER_A_ID)
    fun `complex import restores own data and links but handles external correctly`() {
        val accessToken = serviceTokenService.generateAccessToken(UUID.fromString(USER_A_ID), listOf("USER"))

        val json =
            this::class.java.getResource("/com/r8n/backend/migration/complex_import.json")?.readText()
                ?: throw IllegalStateException("Resource not found")

        val file =
            MockMultipartFile(
                "file",
                "export.json",
                "application/json",
                json.toByteArray(),
            )

        mockMvc
            .perform(
                multipart("/api/import")
                    .header("Authorization", "Bearer $accessToken")
                    .file(file),
            ).andExpect(status().isOk)

        // Verify restoration of OWN opinions
        verify(opinionsApi, times(1)).createOpinion(eq(SUBJECT_1_ID), any(), any(), eq(4.0))
        verify(opinionsApi, times(1)).createOpinion(eq(SUBJECT_2_ID), any(), eq(emptyList()), eq(5.0))

        // Verify restoration of OWN links
        verify(opinionsApi, times(1)).linkComponent(any(), any(), eq(0.5))

        // Verify EXTERNAL opinions are NOT created
        verify(opinionsApi, never()).createOpinion(eq(SUBJECT_3_ID), any(), any(), any())

        // Verify list creation and linking (only own opinions linked)
        verify(opinionListsApi, times(1)).createList(eq("My List A"), eq(OpinionListPrivacyEnumDto.PRIVATE))
        verify(opinionListsApi, atLeastOnce()).linkOpinion(any(), any(), eq(1.0))
        // We expect linkOpinion for A1, but NOT for B1 because it wasn't restored

        // Verify outgoing request is re-created as PENDING (implicitly by calling create)
        verify(outgoingAccessRequestApi, times(1)).create(eq(LIST_B_ID), any(), anyOrNull())
    }

    private fun createOpinionDto(
        id: UUID,
        owner: String,
        subject: UUID,
    ) = OpinionDto(
        id = id,
        owner = UUID.fromString(owner),
        ownerName = "User",
        subject = subject,
        subjectName = "Subject",
        subjective = emptyList(),
        objective = emptyList(),
        mark = 5.0,
        componentMark = null,
        components = emptyList(),
        status = OpinionStatusEnumDto.PUBLISHED,
        timestamp = Instant.now(),
    )

    private fun createOpinionListDto(
        id: UUID,
        owner: String,
        name: String,
    ) = OpinionListDto(
        id = id,
        listName = name,
        owner = UUID.fromString(owner),
        ownerName = "User",
        opinionSummaries = emptyList(),
        privacy = OpinionListPrivacyEnumDto.PRIVATE,
    )
}
