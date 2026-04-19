package com.r8n.backend.opinionlists.integration.configuration

import com.r8n.backend.opinionlists.api.OpinionListsApi
import com.r8n.backend.opinionlists.integration.api.OpinionListsInternalApi
import com.r8n.backend.opinionlists.integration.client.OpinionListInternalRestClient
import com.r8n.backend.opinionlists.integration.client.OpinionListRestClient
import com.r8n.backend.security.SecurityContextTokenInterceptor
import com.r8n.backend.security.ServiceTokenService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class OpinionListsRestClientConfiguration {
    @Bean
    @Qualifier("opinionListsRestBaseClient")
    fun opinionListsRestBaseClient(
        @Value("\${services.opinionLists.url}") baseUrl: String,
        serviceTokenService: ServiceTokenService,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor(SecurityContextTokenInterceptor(serviceTokenService))
            .build()

    @Bean
    fun opinionListRestClient(
        @Qualifier("opinionListsRestBaseClient") opinionListsRestBaseClient: RestClient,
    ): OpinionListsApi = OpinionListRestClient(opinionListsRestBaseClient)

    @Bean
    fun opinionListInternalRestClient(
        @Qualifier("opinionListsRestBaseClient") opinionListsRestBaseClient: RestClient,
    ): OpinionListsInternalApi = OpinionListInternalRestClient(opinionListsRestBaseClient)
}