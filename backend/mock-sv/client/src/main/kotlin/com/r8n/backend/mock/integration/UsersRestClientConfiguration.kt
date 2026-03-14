package com.r8n.backend.users.integration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class UsersRestClientConfiguration {
    @Bean
    fun usersRestClient(
        builder: RestClient.Builder,
        @Value($$"${services.users.url}") baseUrl: String
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .build()

    @Bean
    fun userRestClient(restClient: RestClient): UsersInternalApi =
        UsersRestClient(restClient)
}