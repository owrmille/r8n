package com.r8n.backend.security

import com.r8n.backend.security.SecurityAutoConfiguration.Companion.STUB_ACCESS_TOKEN
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class StubTokenFilter : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.servletPath.startsWith("/auth/")
    }
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val header = request.getHeader("Authorization")

        if (header == "Bearer $STUB_ACCESS_TOKEN") {

            val auth = UsernamePasswordAuthenticationToken(
                "00000000-0000-0000-0000-000000000000",
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )

            SecurityContextHolder.getContext().authentication = auth
            filterChain.doFilter(request, response)
            return
        }

        response.status = HttpServletResponse.SC_UNAUTHORIZED
    }
}