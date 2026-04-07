package com.r8n.backend.security

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class SecurityContextTokenInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null) {
            val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            val originalRequest = attributes?.request
            val authHeader = originalRequest?.getHeader("Authorization")
            if (authHeader != null) {
                request.headers.add("Authorization", authHeader)
            }
        }
        return execution.execute(request, body)
    }
}