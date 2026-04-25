package com.r8n.backend.users

import com.r8n.backend.opinions.api.access.IncomingAccessRequestApi
import com.r8n.backend.opinions.api.access.OutgoingAccessRequestApi
import com.r8n.backend.opinions.integration.api.OpinionListsInternalApi
import com.r8n.backend.users.api.AuthApi.Companion.REFRESH_TOKEN_COOKIE_NAME
import com.r8n.backend.users.api.dto.LoginRequestDto
import com.r8n.backend.users.provider.database.PIIRepository
import com.r8n.backend.users.provider.database.UserRoleAssignmentRepository
import jakarta.persistence.EntityManager
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import tools.jackson.databind.ObjectMapper
import java.sql.Timestamp
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
class AuthIntegrationTest {
    private companion object {
        @Container
        @ServiceConnection
        val postgres: PostgreSQLContainer =
            PostgreSQLContainer(DockerImageName.parse("postgres:15"))
                .withDatabaseName("users")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("db/init-schema.sql")

        // yes, this is a duplicate from AuthApi, left intentional to detect changes
        const val CSRF_PATH = "/api/auth/csrf"
        const val LOGIN_PATH = "/api/auth/login"
        const val REFRESH_PATH = "/api/auth/refresh"
        const val EMAIL = "test@test.test"
        const val PASSWORD = "1234"
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var piiRepository: PIIRepository

    @Autowired
    lateinit var userRoleAssignmentRepository: UserRoleAssignmentRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private fun extractRefreshToken(setCookieHeader: String): String =
        setCookieHeader
            .substringAfter("$REFRESH_TOKEN_COOKIE_NAME=")
            .substringBefore(";")

    @Test
    fun `csrf endpoint initializes xsrf cookie without authentication`() {
        val response =
            mockMvc
                .perform(get(CSRF_PATH))
                .andExpect(status().isOk)
                .andReturn()

        val setCookieHeader = response.response.getHeader(HttpHeaders.SET_COOKIE)!!
        assertTrue(setCookieHeader.contains("XSRF-TOKEN="))
        assertTrue(setCookieHeader.contains("Path=/"))
    }

    @Test
    @Transactional
    fun `login with valid credentials returns tokens`() {
        val pii = piiRepository.findAll().first { it.email == EMAIL }
        val userId = pii.userId
        val encodedPassword = passwordEncoder.encode(PASSWORD)

        entityManager
            .createNativeQuery("UPDATE users.users SET password_hash = :passwordHash WHERE id = :userId")
            .setParameter("passwordHash", encodedPassword)
            .setParameter("userId", userId)
            .executeUpdate()

        entityManager
            .createNativeQuery(
                "INSERT INTO users.users_role_assignments (id, \"user\", role, granted_by, timestamp) VALUES (:id, :userId, :role, :grantedBy, :timestamp)",
            ).setParameter("id", UUID.randomUUID())
            .setParameter("userId", userId)
            .setParameter("role", "ADMIN")
            .setParameter("grantedBy", userId)
            .setParameter("timestamp", Instant.now())
            .executeUpdate()

        entityManager.clear()

        val loginRequest = LoginRequestDto(EMAIL, PASSWORD)
        val beforeLogin = Instant.now()

        val response =
            mockMvc
                .perform(
                    post(LOGIN_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.expiresInMilliseconds").value(3600000))
                .andReturn()

        val setCookieHeader = response.response.getHeader(HttpHeaders.SET_COOKIE)!!
        assertTrue(setCookieHeader.contains("$REFRESH_TOKEN_COOKIE_NAME="))
        assertTrue(setCookieHeader.contains("HttpOnly"))
        assertTrue(setCookieHeader.contains("Path=/api/auth"))

        val lastSeenAt =
            jdbcTemplate.queryForObject(
                "SELECT last_seen_at FROM users.users WHERE id = ?",
                Instant::class.java,
                userId,
            )
        assertTrue(lastSeenAt != null, "last_seen_at should be set after login")
        assertTrue(!lastSeenAt!!.isBefore(beforeLogin), "last_seen_at should be updated after login")
    }

    @Test
    fun `login with invalid password returns 401`() {
        val loginRequest = LoginRequestDto(EMAIL, "wrong")

        mockMvc
            .perform(
                post(LOGIN_PATH)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `login with unknown user returns 401`() {
        val loginRequest = LoginRequestDto("unknown@test.test", PASSWORD)

        mockMvc
            .perform(
                post(LOGIN_PATH)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @Transactional
    fun `refresh token rotation works`() {
        val pii = piiRepository.findAll().first { it.email == EMAIL }
        val userId = pii.userId
        val encodedPassword = passwordEncoder.encode(PASSWORD)

        entityManager
            .createNativeQuery("UPDATE users.users SET password_hash = :passwordHash WHERE id = :userId")
            .setParameter("passwordHash", encodedPassword)
            .setParameter("userId", userId)
            .executeUpdate()

        entityManager.clear()

        val loginRequest = LoginRequestDto(EMAIL, PASSWORD)

        // 1. Login to get the first refresh token
        val loginResponse =
            mockMvc
                .perform(
                    post(LOGIN_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                ).andExpect(status().isOk)
                .andReturn()

        val firstRefreshToken = extractRefreshToken(loginResponse.response.getHeader(HttpHeaders.SET_COOKIE)!!)
        val staleLastSeenAt = Instant.parse("2024-01-01T00:00:00Z")
        jdbcTemplate.update(
            "UPDATE users.users SET last_seen_at = ? WHERE id = ?",
            Timestamp.from(staleLastSeenAt),
            userId,
        )
        val beforeRefresh = Instant.now()

        // 2. First refresh
        val firstRefreshResponse =
            mockMvc
                .perform(
                    post(REFRESH_PATH)
                        .with(csrf())
                        .cookie(Cookie(REFRESH_TOKEN_COOKIE_NAME, firstRefreshToken)),
                ).andExpect(status().isOk)
                .andReturn()

        val secondRefreshToken = extractRefreshToken(firstRefreshResponse.response.getHeader(HttpHeaders.SET_COOKIE)!!)
        assertTrue(firstRefreshToken != secondRefreshToken)
        val refreshedLastSeenAt =
            jdbcTemplate.queryForObject(
                "SELECT last_seen_at FROM users.users WHERE id = ?",
                Instant::class.java,
                userId,
            )
        assertTrue(refreshedLastSeenAt != null, "last_seen_at should be set after refresh")
        assertTrue(!refreshedLastSeenAt!!.isBefore(beforeRefresh), "last_seen_at should be updated after refresh")

        // 3. Second refresh with a NEW token works
        mockMvc
            .perform(
                post(REFRESH_PATH)
                    .with(csrf())
                    .cookie(Cookie(REFRESH_TOKEN_COOKIE_NAME, secondRefreshToken)),
            ).andExpect(status().isOk)

        // 4. Reuse the first refresh token - should FAIL and be rejected (Compromise detection)
        mockMvc
            .perform(
                post(REFRESH_PATH)
                    .with(csrf())
                    .cookie(Cookie(REFRESH_TOKEN_COOKIE_NAME, firstRefreshToken)),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `login requires CSRF`() {
        val loginRequest = LoginRequestDto(EMAIL, PASSWORD)

        mockMvc
            .perform(
                post(LOGIN_PATH)
                    // No CSRF
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `refresh without cookie returns 401`() {
        mockMvc
            .perform(
                post(REFRESH_PATH)
                    .with(csrf()),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh with invalid cookie returns 401`() {
        mockMvc
            .perform(
                post(REFRESH_PATH)
                    .with(csrf())
                    .cookie(Cookie(REFRESH_TOKEN_COOKIE_NAME, "invalid-refresh-token")),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `logout clears refresh token cookie`() {
        val response =
            mockMvc
                .perform(
                    post("/api/auth/logout")
                        .with(csrf())
                        .cookie(Cookie(REFRESH_TOKEN_COOKIE_NAME, "refresh-token-to-clear")),
                ).andExpect(status().isOk)
                .andReturn()

        val setCookieHeader = response.response.getHeader(HttpHeaders.SET_COOKIE)!!
        assertTrue(setCookieHeader.contains("$REFRESH_TOKEN_COOKIE_NAME="))
        assertTrue(setCookieHeader.contains("Max-Age=0"))
        assertTrue(setCookieHeader.contains("HttpOnly"))
        assertTrue(setCookieHeader.contains("Path=/api/auth"))
    }
}