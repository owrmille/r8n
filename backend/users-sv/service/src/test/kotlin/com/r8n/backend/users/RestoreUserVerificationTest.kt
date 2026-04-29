package com.r8n.backend.users

import com.r8n.backend.opinions.api.access.IncomingAccessRequestApi
import com.r8n.backend.opinions.api.access.OutgoingAccessRequestApi
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.api.dto.UserStatusEnumDto
import com.r8n.backend.users.domain.UserStatusEnum
import com.r8n.backend.users.integration.api.dto.UserDto
import com.r8n.backend.users.persistence.PIIPersistence
import com.r8n.backend.users.persistence.UserPersistence
import com.r8n.backend.users.provider.database.PIIRepository
import com.r8n.backend.users.provider.database.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestObjectMapperConfiguration::class)
@MockitoBean(
    types = [
        IncomingAccessRequestApi::class,
        OutgoingAccessRequestApi::class,
        OpinionListsInternalApi::class,
    ],
)
class RestoreUserVerificationTest {
    companion object {
        @Suppress("unused")
        @Container
        @ServiceConnection
        val postgres =
            PostgreSQLContainer("postgres:15")
                .withDatabaseName("users")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("db/init-schema.sql")
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var serviceTokenService: ServiceTokenService

    @Autowired
    lateinit var objectMapper: JsonMapper

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var piiRepository: PIIRepository

    @Test
    fun `restoreUser should update existing user by email and return 200 OK`() {
        // Arrange: Create an existing user
        val existingUserId = UUID.randomUUID()
        val email = "existing@example.com"
        val existingUser =
            UserPersistence(
                id = existingUserId,
                status = UserStatusEnum.ACTIVE,
                statusTimestamp = Instant.now(),
                lastSeenAt = Instant.now(),
                passwordHash = "hash",
            )
        userRepository.save(existingUser)

        val pii =
            PIIPersistence(
                userId = existingUserId,
                name = "Original Name",
                email = email,
                phone = null,
                about = null,
                location = null,
            )
        piiRepository.save(pii)

        val token = serviceTokenService.generateServiceToken()
        val userDto =
            UserDto(
                name = "New Name Attempt",
                email = email,
                status = UserStatusEnumDto.SUSPENDED,
                statusTimestamp = Instant.now(),
                consents = emptyList(),
            )

        // Act
        val result =
            mockMvc
                .perform(
                    post("/api/internal/users/import")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)),
                ).andExpect(status().isOk)
                .andReturn()

        val returnedIdString = result.response.contentAsString.replace("\"", "")
        val returnedId = UUID.fromString(returnedIdString)

        // Assert
        assertEquals(existingUserId, returnedId)

        val updatedUser = userRepository.findById(existingUserId).get()
        assertEquals(UserStatusEnum.SUSPENDED, updatedUser.status)
        assertEquals("hash", updatedUser.passwordHash)

        val updatedPii = piiRepository.findById(existingUserId).get()
        assertEquals("Original Name", updatedPii.name)
        assertEquals(email, updatedPii.email)
    }
}
