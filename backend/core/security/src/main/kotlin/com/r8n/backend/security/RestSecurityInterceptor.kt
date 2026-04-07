package com.r8n.backend.security

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.core.context.SecurityContextHolder

class RestSecurityInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated) {
            // In a real app we'd get the token from details or credentials.
            // For this stub implementation, we just use the constant.
            request.headers.add("Authorization", "Bearer ${SecurityAutoConfiguration.STUB_ACCESS_TOKEN}")
        }
        return execution.execute(request, body)
    }
}