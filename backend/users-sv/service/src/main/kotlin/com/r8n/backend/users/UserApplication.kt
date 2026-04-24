package com.r8n.backend.users

import com.r8n.backend.messaging.integration.MessagingRestClientConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@Import(
    MessagingRestClientConfiguration::class,
)
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}