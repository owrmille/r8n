package com.r8n.backend.users.integration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class UsersRestClientConfiguration {
    @Bean
    @Qualifier("usersRestClient")
    fun usersRestClient(
        @Value("${"$"}{services.users.url}") baseUrl: String
    ): RestClient =
        RestClient.builder()
            .baseUrl(baseUrl)
            .build()

    @Bean
    fun userRestClient(@Qualifier("usersRestClient") restClient: RestClient): UsersInternalApi =
        UsersRestClient(restClient)
}