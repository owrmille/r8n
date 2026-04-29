package com.r8n.backend.opinions.access

import com.r8n.backend.core.api.PageResponseDto
import com.r8n.backend.opinions.TestObjectMapperConfiguration
import com.r8n.backend.opinions.access.database.AccessRequestRepository
import com.r8n.backend.opinions.api.access.dto.AccessRequestDto
import com.r8n.backend.opinions.api.access.dto.AccessRequestIntentDto
import com.r8n.backend.opinions.api.access.dto.RequestStatusEnumDto
import com.r8n.backend.opinions.lists.database.OpinionListRepository
import com.r8n.backend.opinions.lists.database.OpinionsToOpinionListsRepository
import com.r8n.backend.opinions.lists.domain.OpinionListPrivacyEnum
import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
import com.r8n.backend.opinions.lists.persistence.OpinionsToOpinionListsPersistence
import com.r8n.backend.opinions.opinions.database.OpinionNoteRepository
import com.r8n.backend.opinions.opinions.database.OpinionRepository
import com.r8n.backend.opinions.opinions.database.OpinionSubjectRepository
import com.r8n.backend.opinions.opinions.database.ReferentRepository
import com.r8n.backend.opinions.opinions.domain.OpinionStatusEnum
import com.r8n.backend.opinions.opinions.persistence.OpinionNotePersistence
import com.r8n.backend.opinions.opinions.persistence.OpinionNoteTypeEnum
import com.r8n.backend.opinions.opinions.persistence.OpinionPersistence
import com.r8n.backend.opinions.opinions.persistence.OpinionSubjectPersistence
import com.r8n.backend.opinions.opinions.persistence.ReferentPersistence
import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
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
class AccessRequestIntegrationTest {
    private companion object {
        val OWNER_ID: UUID = UUID.randomUUID()
        val REQUESTER_ID: UUID = UUID.randomUUID()

        @Suppress("unused") // used to store test database container
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

    @Autowired
    lateinit var serviceTokenService: ServiceTokenService

    @Autowired
    lateinit var opinionRepository: OpinionRepository

    @Autowired
    lateinit var accessRequestRepository: AccessRequestRepository

    @Autowired
    lateinit var opinionSubjectRepository: OpinionSubjectRepository

    @Autowired
    lateinit var referentRepository: ReferentRepository

    @Autowired
    lateinit var opinionNoteRepository: OpinionNoteRepository

    @Autowired
    lateinit var opinionListRepository: OpinionListRepository

    @Autowired
    lateinit var opinionsToOpinionListsRepository: OpinionsToOpinionListsRepository

    @MockitoBean
    lateinit var usersInternalApi: UsersInternalApi

    lateinit var ownerToken: String
    lateinit var requesterToken: String
    lateinit var opinionId: UUID
    lateinit var listId: UUID

    @BeforeEach
    fun setUp() {
        whenever(usersInternalApi.getUserName(any())).thenReturn("Test User")

        ownerToken = serviceTokenService.generateAccessToken(OWNER_ID, listOf("USER"))!!
        requesterToken = serviceTokenService.generateAccessToken(REQUESTER_ID, listOf("USER"))!!

        // Create a referent
        val referent =
            referentRepository.save(
                ReferentPersistence(
                    name = "Test Cafe",
                    address = "Berlin",
                    latitude = 52.52,
                    longitude = 13.40,
                    referentGroup = UUID.randomUUID(),
                ),
            )

        // Create a subject
        val subject =
            opinionSubjectRepository.save(
                OpinionSubjectPersistence(
                    name = "Service",
                    referent = referent.id!!,
                ),
            )

        // Create an opinion owned by the owner
        val opinion =
            opinionRepository.save(
                OpinionPersistence(
                    owner = OWNER_ID,
                    subject = subject.id!!,
                    mark = 4.5,
                    status = OpinionStatusEnum.PUBLISHED,
                    timestamp = Instant.now(),
                ),
            )
        opinionId = opinion.id!!

        // Create opinion notes
        opinionNoteRepository.save(
            OpinionNotePersistence(
                opinionId = opinionId,
                type = OpinionNoteTypeEnum.OBJECTIVE,
                description = "Objective note",
            ),
        )
        opinionNoteRepository.save(
            OpinionNotePersistence(
                opinionId = opinionId,
                type = OpinionNoteTypeEnum.SUBJECTIVE,
                description = "Subjective note",
            ),
        )

        // Create an opinion list owned by the owner
        val list =
            opinionListRepository.save(
                OpinionListPersistence(
                    owner = OWNER_ID,
                    name = "Private List",
                    privacy = OpinionListPrivacyEnum.PRIVATE,
                ),
            )
        listId = list.id!!

        // Link opinion to list
        opinionsToOpinionListsRepository.save(
            OpinionsToOpinionListsPersistence(
                opinionList = listId,
                opinion = opinionId,
                weight = 1.0,
            ),
        )
    }

    @Test
    @WithMockUser
    fun `access request lifecycle works`() {
        // Step 1: Requester tries to access the opinion and fails (403 Forbidden)
        mockMvc
            .perform(
                get("/api/opinions/$opinionId")
                    .header("Authorization", "Bearer $requesterToken")
                    .with(csrf()),
            ).andExpect(status().isForbidden)

        // Step 2: Requester sends an access request to the list
        val createResult =
            mockMvc
                .perform(
                    post("/api/access-requests/outgoing/create/$listId")
                        .header("Authorization", "Bearer $requesterToken")
                        .with(csrf()),
                ).andExpect(status().isOk)
                .andReturn()

        val request: AccessRequestDto = objectMapper.readValue(createResult.response.contentAsString)
        val requestId = request.id

        // Step 3: Requester tries to access the opinion again and fails (still 403 Forbidden)
        mockMvc
            .perform(
                get("/api/opinions/$opinionId")
                    .header("Authorization", "Bearer $requesterToken")
                    .with(csrf()),
            ).andExpect(status().isForbidden)

        // Step 4: Owner hides the request
        mockMvc
            .perform(
                post("/api/access-requests/incoming/$requestId/hide")
                    .header("Authorization", "Bearer $ownerToken")
                    .with(csrf()),
            ).andExpect(status().isOk)

        // Step 4b: Requester asks for its status, and it shows up as pending for him, not hidden
        val outgoingResult =
            mockMvc
                .perform(
                    get("/api/access-requests/outgoing")
                        .header("Authorization", "Bearer $requesterToken")
                        .param("forListId", listId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .with(csrf()),
                ).andExpect(status().isOk)
                .andReturn()

        val outgoingPage: PageResponseDto<AccessRequestDto> =
            objectMapper.readValue(
                outgoingResult.response.contentAsString,
            )
        val hiddenRequest = outgoingPage.items.find { it.id == requestId }
        assertEquals(
            RequestStatusEnumDto.SENT,
            hiddenRequest?.status,
            "Requester should see SENT status even if HIDDEN by owner",
        )

        // Step 5: Requester tries to access the opinion and fails (403 Forbidden)
        mockMvc
            .perform(
                get("/api/opinions/$opinionId")
                    .header("Authorization", "Bearer $requesterToken")
                    .with(csrf()),
            ).andExpect(status().isForbidden)

        // Step 6: Owner accepts the request
        mockMvc
            .perform(
                post("/api/access-requests/incoming/$requestId/accept")
                    .header("Authorization", "Bearer $ownerToken")
                    .with(csrf()),
            ).andExpect(status().isOk)

        // Step 7: Requester successfully accesses the opinion (200 OK)
        mockMvc
            .perform(
                get("/api/opinions/$opinionId")
                    .header("Authorization", "Bearer $requesterToken")
                    .with(csrf()),
            ).andExpect(status().isOk)

        // Step 8: Owner rejects the request (which should revoke access)
        mockMvc
            .perform(
                post("/api/access-requests/incoming/$requestId/decline")
                    .header("Authorization", "Bearer $ownerToken")
                    .with(csrf()),
            ).andExpect(status().isOk)

        // Step 9: Requester tries to access the opinion and fails (403 Forbidden)
        mockMvc
            .perform(
                get("/api/opinions/$opinionId")
                    .header("Authorization", "Bearer $requesterToken")
                    .with(csrf()),
            ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser
    fun `create with intent COPY persists COPY and no targetListId`() {
        val result =
            mockMvc
                .perform(
                    post("/api/access-requests/outgoing/create/$listId")
                        .header("Authorization", "Bearer $requesterToken")
                        .param("intent", "COPY")
                        .with(csrf()),
                ).andExpect(status().isOk)
                .andReturn()

        val dto: AccessRequestDto = objectMapper.readValue(result.response.contentAsString)
        assertEquals(AccessRequestIntentDto.COPY, dto.intent)
        assertNull(dto.targetListId)
    }

    @Test
    @WithMockUser
    fun `create with intent MERGE and valid owned target persists both fields`() {
        val targetList =
            opinionListRepository.save(
                OpinionListPersistence(
                    owner = REQUESTER_ID,
                    name = "Requester's Own List",
                    privacy = OpinionListPrivacyEnum.PRIVATE,
                ),
            )

        val result =
            mockMvc
                .perform(
                    post("/api/access-requests/outgoing/create/$listId")
                        .header("Authorization", "Bearer $requesterToken")
                        .param("intent", "MERGE")
                        .param("targetListId", targetList.id.toString())
                        .with(csrf()),
                ).andExpect(status().isOk)
                .andReturn()

        val dto: AccessRequestDto = objectMapper.readValue(result.response.contentAsString)
        assertEquals(AccessRequestIntentDto.MERGE, dto.intent)
        assertEquals(targetList.id, dto.targetListId)
        assertNotNull(dto.targetListId)
    }

    @Test
    @WithMockUser
    fun `create with intent MERGE without targetListId returns 400`() {
        mockMvc
            .perform(
                post("/api/access-requests/outgoing/create/$listId")
                    .header("Authorization", "Bearer $requesterToken")
                    .param("intent", "MERGE")
                    .with(csrf()),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `create with intent MERGE targeting a list owned by someone else returns 403`() {
        val foreignTargetList =
            opinionListRepository.save(
                OpinionListPersistence(
                    owner = OWNER_ID,
                    name = "Owner's other list",
                    privacy = OpinionListPrivacyEnum.PRIVATE,
                ),
            )

        mockMvc
            .perform(
                post("/api/access-requests/outgoing/create/$listId")
                    .header("Authorization", "Bearer $requesterToken")
                    .param("intent", "MERGE")
                    .param("targetListId", foreignTargetList.id.toString())
                    .with(csrf()),
            ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser
    fun `create with intent COPY but targetListId set returns 400`() {
        val targetList =
            opinionListRepository.save(
                OpinionListPersistence(
                    owner = REQUESTER_ID,
                    name = "Stray target",
                    privacy = OpinionListPrivacyEnum.PRIVATE,
                ),
            )

        mockMvc
            .perform(
                post("/api/access-requests/outgoing/create/$listId")
                    .header("Authorization", "Bearer $requesterToken")
                    .param("intent", "COPY")
                    .param("targetListId", targetList.id.toString())
                    .with(csrf()),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `MERGE auto-execute end-to-end - request, accept, sync from a SEARCHABLE source`() {
        // Owner has a SEARCHABLE list — the kind that surfaces in Discover today.
        val sourceList =
            opinionListRepository.save(
                OpinionListPersistence(
                    owner = OWNER_ID,
                    name = "Owner's searchable list",
                    privacy = OpinionListPrivacyEnum.SEARCHABLE,
                ),
            )

        // Requester owns the merge destination.
        val requesterDestList =
            opinionListRepository.save(
                OpinionListPersistence(
                    owner = REQUESTER_ID,
                    name = "Requester's destination",
                    privacy = OpinionListPrivacyEnum.PRIVATE,
                ),
            )

        // Sanity: SEARCHABLE source without access → 403 (existence is public).
        mockMvc
            .perform(
                post("/api/opinion-lists/${requesterDestList.id}/sync")
                    .header("Authorization", "Bearer $requesterToken")
                    .param("addedListId", sourceList.id.toString())
                    .with(csrf()),
            ).andExpect(status().isForbidden)

        // Requester creates an access request with intent=MERGE.
        val createResult =
            mockMvc
                .perform(
                    post("/api/access-requests/outgoing/create/${sourceList.id}")
                        .header("Authorization", "Bearer $requesterToken")
                        .param("intent", "MERGE")
                        .param("targetListId", requesterDestList.id.toString())
                        .with(csrf()),
                ).andExpect(status().isOk)
                .andReturn()
        val requestId = objectMapper.readValue<AccessRequestDto>(createResult.response.contentAsString).id

        // Owner accepts.
        mockMvc
            .perform(
                post("/api/access-requests/incoming/$requestId/accept")
                    .header("Authorization", "Bearer $ownerToken")
                    .with(csrf()),
            ).andExpect(status().isOk)

        // The frontend auto-execute will now POST this sync — it must succeed.
        mockMvc
            .perform(
                post("/api/opinion-lists/${requesterDestList.id}/sync")
                    .header("Authorization", "Bearer $requesterToken")
                    .param("addedListId", sourceList.id.toString())
                    .with(csrf()),
            ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser
    fun `create with default intent (no param) defaults to NONE`() {
        val result =
            mockMvc
                .perform(
                    post("/api/access-requests/outgoing/create/$listId")
                        .header("Authorization", "Bearer $requesterToken")
                        .with(csrf()),
                ).andExpect(status().isOk)
                .andReturn()

        val dto: AccessRequestDto = objectMapper.readValue(result.response.contentAsString)
        assertEquals(AccessRequestIntentDto.NONE, dto.intent)
        assertNull(dto.targetListId)
    }
}
