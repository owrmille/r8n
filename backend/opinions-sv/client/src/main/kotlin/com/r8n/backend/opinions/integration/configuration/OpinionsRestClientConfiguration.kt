package com.r8n.backend.opinions.integration.configuration

import com.r8n.backend.opinions.api.OpinionsApi
import com.r8n.backend.opinions.integration.api.OpinionsInternalApi
import com.r8n.backend.opinions.integration.client.OpinionsRestClient
import com.r8n.backend.opinions.integration.client.OpinionsInternalRestClient
import com.r8n.backend.security.SecurityContextTokenInterceptor
import com.r8n.backend.security.ServiceTokenService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class OpinionsRestClientConfiguration {
    @Bean
    @Qualifier("opinionsRestBaseClient")
    fun opinionsRestBaseClient(
        @Value("\${services.opinions.url}") baseUrl: String,
        serviceTokenService: ServiceTokenService,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor(SecurityContextTokenInterceptor(serviceTokenService))
            .build()

    @Bean
    fun opinionRestClient(
        @Qualifier("opinionsRestBaseClient") restClient: RestClient,
    ): OpinionsApi = OpinionsRestClient(restClient)

    @Bean
    fun opinionsInternalRestClient(
        @Qualifier("opinionsRestBaseClient") restClient: RestClient,
    ): OpinionsInternalApi = OpinionsInternalRestClient(restClient)
}