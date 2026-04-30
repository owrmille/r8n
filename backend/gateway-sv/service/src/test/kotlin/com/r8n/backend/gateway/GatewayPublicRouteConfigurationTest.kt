package com.r8n.backend.gateway

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class GatewayPublicRouteConfigurationTest {
    @Test
    fun `all public routes are exposed with api key protection in every gateway config`() {
        val gatewayConfigs =
            listOf(
                Path.of("src/main/resources/application.yml"),
                Path.of("src/main/resources/application-local.yml"),
                Path.of("src/test/resources/application-test.yml"),
                Path.of("src/test/resources/application-test-e2e.yml"),
            )

        val publicRoutes = listOf("public_opinions", "public_subjects", "public_users", "public_messaging")

        gatewayConfigs.forEach { config ->
            val content = Files.readString(config)

            publicRoutes.forEach { route ->
                assertThat(content)
                    .describedAs("$config exposes $route through the public API namespace")
                    .contains("id: $route")
                    .contains("ApiKeyAuthenticationFilter")
                    .contains("RateLimitingFilter")
            }
        }
    }

    @Test
    fun `all v3 api-docs routes are correctly configured in every gateway config`() {
        val gatewayConfigs =
            listOf(
                Path.of("src/main/resources/application.yml"),
                Path.of("src/main/resources/application-local.yml"),
                Path.of("src/test/resources/application-test.yml"),
                Path.of("src/test/resources/application-test-e2e.yml"),
            )

        val docRoutes =
            listOf("opinions_v3_api_docs", "users_v3_api_docs", "migration_v3_api_docs", "messaging_v3_api_docs")

        gatewayConfigs.forEach { config ->
            val content = Files.readString(config)

            docRoutes.forEach { route ->
                assertThat(content)
                    .describedAs("$config configures $route")
                    .contains("id: $route")
                    .contains("RewritePath")
                    .contains("/v3/api-docs")
            }
        }
    }
}
