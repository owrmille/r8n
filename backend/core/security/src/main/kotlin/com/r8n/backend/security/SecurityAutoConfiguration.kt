package com.r8n.backend.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityAutoConfiguration {
    companion object {
        const val STUB_LOGIN = "test"
        const val STUB_PASSWORD = "1234"

        const val STUB_ACCESS_TOKEN = "stub-access-token-123"
        const val STUB_REFRESH_TOKEN = "stub-refresh-token-456"
    }

    @Bean
    @ConditionalOnMissingBean
    fun restSecurityInterceptor(): RestSecurityInterceptor {
        return RestSecurityInterceptor()
    }

    @Bean
    @ConditionalOnMissingBean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                StubTokenFilter(),
                UsernamePasswordAuthenticationFilter::class.java
            )
            //.oauth2ResourceServer(oauth -> oauth.jwt())
            .build()
    }
}