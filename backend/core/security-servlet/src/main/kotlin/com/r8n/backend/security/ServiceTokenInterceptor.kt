package com.r8n.backend.security

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class ServiceTokenInterceptor(
    private val serviceTokenService: ServiceTokenService,
) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        serviceTokenService.generateServiceToken()?.let { token ->
            request.headers.add("Authorization", "Bearer $token")
        }
        return execution.execute(request, body)
    }
}
