package com.r8n.backend.mock.integration.configuration

import com.r8n.backend.mock.api.MessagingApi
import com.r8n.backend.mock.api.OpinionListApi
import com.r8n.backend.mock.api.RecommendationApi
import com.r8n.backend.mock.api.SelectorApi
import com.r8n.backend.mock.integration.api.OpinionListInternalApi
import com.r8n.backend.mock.integration.client.MessagingRestClient
import com.r8n.backend.mock.integration.client.OpinionListInternalRestClient
import com.r8n.backend.mock.integration.client.OpinionListRestClient
import com.r8n.backend.mock.integration.client.RecommendationRestClient
import com.r8n.backend.mock.integration.client.SelectorRestClient
import com.r8n.backend.security.SecurityContextTokenInterceptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class MockRestClientConfiguration {
    @Bean
    @Qualifier("mockRestBaseClient")
    fun mockRestBaseClient(
        @Value("\${services.mock.url}") baseUrl: String,
        serviceTokenService: com.r8n.backend.security.ServiceTokenService,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor(SecurityContextTokenInterceptor(serviceTokenService))
            .build()

    @Bean
    fun opinionListRestClient(
        @Qualifier("mockRestBaseClient") mockRestBaseClient: RestClient,
    ): OpinionListApi = OpinionListRestClient(mockRestBaseClient)

    @Bean
    fun recommendationsRestClient(
        @Qualifier("mockRestBaseClient") mockRestBaseClient: RestClient,
    ): RecommendationApi = RecommendationRestClient(mockRestBaseClient)

    @Bean
    fun selectorRestClient(
        @Qualifier("mockRestBaseClient") mockRestBaseClient: RestClient,
    ): SelectorApi = SelectorRestClient(mockRestBaseClient)

    @Bean
    fun messagingRestClient(
        @Qualifier("mockRestBaseClient") mockRestBaseClient: RestClient,
    ): MessagingApi = MessagingRestClient(mockRestBaseClient)

    @Bean
    fun opinionListInternalRestClient(
        @Qualifier("mockRestBaseClient") mockRestBaseClient: RestClient,
    ): OpinionListInternalApi = OpinionListInternalRestClient(mockRestBaseClient)
}