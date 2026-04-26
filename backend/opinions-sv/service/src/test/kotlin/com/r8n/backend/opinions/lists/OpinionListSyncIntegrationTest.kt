package com.r8n.backend.opinions.lists

import com.r8n.backend.opinions.TestObjectMapperConfiguration
import com.r8n.backend.opinions.access.database.AccessRequestRepository
import com.r8n.backend.opinions.access.domain.RequestStatusEnum
import com.r8n.backend.opinions.api.lists.dto.OpinionListDto
import com.r8n.backend.opinions.lists.database.OpinionListRepository
import com.r8n.backend.opinions.lists.database.OpinionListSyncRepository
import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
class OpinionListSyncIntegrationTest {
    private companion object {
        val BERNARD_ID: UUID = UUID.fromString("10101010-1010-1010-1010-101010101010")
        val ANNA_ID: UUID = UUID.fromString("20202020-2020-2020-2020-202020202020")

        val BERNARD_LIST_ID: UUID = UUID.fromString("70000000-0000-0000-0000-000000000001")
        val ANNA_LIST_ID: UUID = UUID.fromString("70000000-0000-0000-0000-000000000003")

        val PRESEEDED_REQUEST_ID: UUID = UUID.fromString("30000000-0000-0000-0000-300000000006")

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
    lateinit var objectMapper: JsonMapper

    @Autowired
    lateinit var serviceTokenService: ServiceTokenService

    @Autowired
    lateinit var opinionListRepository: OpinionListRepository

    @Autowired
    lateinit var opinionListSyncRepository: OpinionListSyncRepository

    @Autowired
    lateinit var accessRequestRepository: AccessRequestRepository

    @MockitoBean
    lateinit var usersInternalApi: UsersInternalApi

    lateinit var requesterToken: String

    @BeforeEach
    fun setUp() {
        requesterToken = "Bearer " + serviceTokenService.generateAccessToken(ANNA_ID, listOf("USER"))

        whenever(usersInternalApi.isAnyModerator(any())).thenReturn(false)
        whenever(usersInternalApi.isHumanModerator(any())).thenReturn(false)
        whenever(usersInternalApi.getUserName(any())).thenReturn("Anna Müller")
    }

    @Test
    fun `sync and unsync with opinion list works`() {
        // 1. Bernard's list is the source (preseeded)
        // 2. Anna's list is the destination (preseeded)

        // 3. Sync without approved access request - should fail (it's currently SENT)
        mockMvc
            .perform(
                post("/api/opinion-lists/$ANNA_LIST_ID/sync")
                    .header("Authorization", requesterToken)
                    .param("addedListId", BERNARD_LIST_ID.toString()),
            ).andExpect(status().isForbidden)

        // 4. Approve the preseeded access request
        val request = accessRequestRepository.findById(PRESEEDED_REQUEST_ID).get()
        request.status = RequestStatusEnum.ACCEPTED
        accessRequestRepository.save(request)

        // 5. Sync with access request - should work
        val syncResult =
            mockMvc
                .perform(
                    post("/api/opinion-lists/$ANNA_LIST_ID/sync")
                        .header("Authorization", requesterToken)
                        .param("addedListId", BERNARD_LIST_ID.toString()),
                ).andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
                .let { objectMapper.readValue<OpinionListDto>(it) }

        assertEquals(ANNA_LIST_ID, syncResult.id)
        val syncPersistence =
            opinionListSyncRepository.findByDestinationListAndSourceList(
                ANNA_LIST_ID,
                BERNARD_LIST_ID,
            )
        assertTrue(syncPersistence != null)
        assertEquals(1.0, syncPersistence?.weight)

        // 5.1 Update weight
        mockMvc
            .perform(
                post("/api/opinion-lists/$ANNA_LIST_ID/sync")
                    .header("Authorization", requesterToken)
                    .param("addedListId", BERNARD_LIST_ID.toString())
                    .param("weight", "0.5"),
            ).andExpect(status().isOk)

        val updatedSyncPersistence =
            opinionListSyncRepository.findByDestinationListAndSourceList(
                ANNA_LIST_ID,
                BERNARD_LIST_ID,
            )
        assertEquals(0.5, updatedSyncPersistence?.weight)

        // 6. Unsync
        val unsyncResult =
            mockMvc
                .perform(
                    post("/api/opinion-lists/$ANNA_LIST_ID/unsync")
                        .header("Authorization", requesterToken)
                        .param("removedListId", BERNARD_LIST_ID.toString()),
                ).andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
                .let { objectMapper.readValue<OpinionListDto>(it) }

        assertEquals(ANNA_LIST_ID, unsyncResult.id)
        assertTrue(
            opinionListSyncRepository.findByDestinationListAndSourceList(ANNA_LIST_ID, BERNARD_LIST_ID) == null,
        )
    }

    @Test
    fun `cannot sync to a list you do not own`() {
        mockMvc
            .perform(
                post("/api/opinion-lists/$BERNARD_LIST_ID/sync")
                    .header("Authorization", requesterToken)
                    .param("addedListId", ANNA_LIST_ID.toString()),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `sync with invalid weight should return bad request`() {
        // Approve the preseeded access request
        val request = accessRequestRepository.findById(PRESEEDED_REQUEST_ID).get()
        request.status = RequestStatusEnum.ACCEPTED
        accessRequestRepository.save(request)

        // Weight < 0
        mockMvc
            .perform(
                post("/api/opinion-lists/$ANNA_LIST_ID/sync")
                    .header("Authorization", requesterToken)
                    .param("addedListId", BERNARD_LIST_ID.toString())
                    .param("weight", "-0.1"),
            ).andExpect(status().isBadRequest)

        // Weight > 1
        mockMvc
            .perform(
                post("/api/opinion-lists/$ANNA_LIST_ID/sync")
                    .header("Authorization", requesterToken)
                    .param("addedListId", BERNARD_LIST_ID.toString())
                    .param("weight", "1.1"),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `complex ownership and sync schema works`() {
        // u1 looks at his l11
        // u1: l11(r11, r12)
        // l21(r23, r24), l22(empty), l31(r31) synced to l11
        // Total for l11: r11(s1), r12(s2), r23(s3), r24(s4), r31(s1)

        val u1Token = "Bearer " + serviceTokenService.generateAccessToken(ANNA_ID, listOf("USER"))
        val l11Id = UUID.fromString("80000000-0000-0000-0000-000000000111")

        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists/$l11Id")
                        .header("Authorization", u1Token),
                ).andExpect(status().isOk)
                .andReturn()

        val json = result.response.contentAsString
        val list = objectMapper.readValue(json, OpinionListDto::class.java)

        // s1 (r11, r31), s2 (r12), s3 (r23), s4 (r24)
        assertThat(list.opinionSummaries).hasSize(4)

        val s1 = list.opinionSummaries.find { it.subjectName == "Subject 1" }!!
        assertThat(s1.ownMark).isEqualTo(1.1)
        assertThat(s1.opinions).hasSize(2)
        assertThat(s1.opinions.map { it.opinion }).containsExactlyInAnyOrder(
            UUID.fromString("40000000-0000-0000-0000-000000000011"),
            UUID.fromString("40000000-0000-0000-0000-000000000031"),
        )

        val s2 = list.opinionSummaries.find { it.subjectName == "Subject 2" }!!
        assertThat(s2.ownMark).isEqualTo(1.2)
        assertThat(s2.opinions).hasSize(1)

        val s3 = list.opinionSummaries.find { it.subjectName == "Subject 3" }!!
        assertThat(s3.ownMark).isNull()
        assertThat(s3.opinions).hasSize(1)
        assertThat(s3.opinions[0].opinion).isEqualTo(UUID.fromString("40000000-0000-0000-0000-000000000023"))

        val s4 = list.opinionSummaries.find { it.subjectName == "Subject 4" }!!
        assertThat(s4.ownMark).isNull()
        assertThat(s4.opinions).hasSize(1)
        assertThat(s4.opinions[0].opinion).isEqualTo(UUID.fromString("40000000-0000-0000-0000-000000000024"))
    }

    @Test
    fun `no transitive connections - only direct reviews are synced`() {
        // u1 looks at l11
        // u1: l11(r11, r12)
        // l31(r31) synced to l11
        // l21(r23, r24) synced to l11
        
        // Total for l11: r11, r12, r31, r23, r24
        
        // l21 has l11 and l12 synced to it.
        // IF it were transitive, l11 would see its own reviews AGAIN via l21, and also r11 via l12 synced to l21.
        
        val u1Token = "Bearer " + serviceTokenService.generateAccessToken(ANNA_ID, listOf("USER"))
        val l11Id = UUID.fromString("80000000-0000-0000-0000-000000000111")

        val result =
            mockMvc
                .perform(
                    get("/api/opinion-lists/$l11Id")
                        .header("Authorization", u1Token),
                ).andExpect(status().isOk)
                .andReturn()

        val json = result.response.contentAsString
        val list = objectMapper.readValue(json, OpinionListDto::class.java)

        // Subject 1 (r11, r31), Subject 2 (r12), Subject 3 (r23), Subject 4 (r24)
        assertThat(list.opinionSummaries).hasSize(4)

        val s1 = list.opinionSummaries.find { it.subjectName == "Subject 1" }!!
        // r11 (direct), r31 (synced from l31)
        // it should NOT have r11 again (synced from l21 <- l11) or r11 (synced from l21 <- l12)
        assertThat(s1.opinions).hasSize(2)
        assertThat(s1.opinions.map { it.opinion }).containsExactlyInAnyOrder(
            UUID.fromString("40000000-0000-0000-0000-000000000011"),
            UUID.fromString("40000000-0000-0000-0000-000000000031"),
        )
    }
}
