package com.r8n.backend.messaging.integration

import com.r8n.backend.messaging.integration.api.MessagingInternalApi
import com.r8n.backend.messaging.integration.client.internal.MessagingInternalRestClient
import com.r8n.backend.security.ServiceTokenInterceptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class MessagingInternalRestClientConfiguration {
    @Bean
    @Qualifier("messagingInternalRestBaseClient")
    fun messagingInternalRestBaseClient(
        @Value("\${services.messaging.url}") baseUrl: String,
        serviceTokenService: com.r8n.backend.security.ServiceTokenService,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor(ServiceTokenInterceptor(serviceTokenService))
            .build()

    @Bean
    fun messagingInternalRestClient(
        @Qualifier("messagingInternalRestBaseClient") messagingInternalRestBaseClient: RestClient,
    ): MessagingInternalApi = MessagingInternalRestClient(messagingInternalRestBaseClient)
}
