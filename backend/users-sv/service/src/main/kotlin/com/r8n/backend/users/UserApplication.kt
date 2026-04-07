package com.r8n.backend.users

import com.r8n.backend.mock.integration.configuration.MockRestClientConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(MockRestClientConfiguration::class)
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}