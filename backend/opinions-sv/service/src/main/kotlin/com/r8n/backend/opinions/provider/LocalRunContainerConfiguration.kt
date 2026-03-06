package com.r8n.backend.opinions.provider

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.PostgreSQLContainer

@Profile("local")
@Configuration
class LocalRunContainerConfiguration {

    @Bean
    @ServiceConnection
    fun postgres(): PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:15")
            .withDatabaseName(System.getenv("DATABASE_OPINIONS_SCHEMA"))
            .withUsername(System.getenv("DATABASE_OPINIONS_USERNAME"))
            .withPassword(System.getenv("DATABASE_OPINIONS_PASSWORD"))
            .withCreateContainerCmdModifier { it.withName("opinions-db") }
}
