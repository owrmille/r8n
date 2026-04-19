package com.r8n.backend.users

import com.r8n.backend.security.ServiceTokenService
import com.r8n.backend.users.persistence.RoleEnumPersistence
import com.r8n.backend.users.persistence.UserRoleAssignmentPersistence
import com.r8n.backend.users.provider.database.UserRoleAssignmentRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Instant
import java.util.UUID

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "services.access.url=http://localhost:8080",
        "services.mock.url=http://localhost:8080",
        "r8n.security.jwt.private-key=-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDJ6v3R6O+WlMvT\n-----END PRIVATE KEY-----",
        "r8n.security.jwt.public-key=-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyur90ejvlpTL0w==\n-----END PUBLIC KEY-----",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ],
)
@Import(TestObjectMapperConfiguration::class)
class RolesInternalIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        val postgres: PostgreSQLContainer =
            PostgreSQLContainer(DockerImageName.parse("postgres:15"))
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
    lateinit var userRoleAssignmentRepository: UserRoleAssignmentRepository

    @Test
    fun `isModerator returns true if user has moderator role`() {
        val userId = UUID.randomUUID()
        val token = serviceTokenService.generateServiceToken()

        userRoleAssignmentRepository.save(
            UserRoleAssignmentPersistence(
                id = UUID.randomUUID(),
                user = userId,
                role = RoleEnumPersistence.MODERATOR,
                grantedBy = UUID.randomUUID(),
                timestamp = Instant.now(),
            ),
        )

        mockMvc
            .perform(
                get("/api/users/$userId/is-moderator")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)
            .andExpect(content().string("true"))
    }

    @Test
    fun `isModerator returns false if user does not have moderator role`() {
        val userId = UUID.randomUUID()
        val token = serviceTokenService.generateServiceToken()

        mockMvc
            .perform(
                get("/api/users/$userId/is-moderator")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)
            .andExpect(content().string("false"))
    }

    @Test
    fun `isAdmin returns true if user has admin role`() {
        val userId = UUID.randomUUID()
        val token = serviceTokenService.generateServiceToken()

        userRoleAssignmentRepository.save(
            UserRoleAssignmentPersistence(
                id = UUID.randomUUID(),
                user = userId,
                role = RoleEnumPersistence.ADMIN,
                grantedBy = UUID.randomUUID(),
                timestamp = Instant.now(),
            ),
        )

        mockMvc
            .perform(
                get("/api/users/$userId/is-admin")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)
            .andExpect(content().string("true"))
    }
}