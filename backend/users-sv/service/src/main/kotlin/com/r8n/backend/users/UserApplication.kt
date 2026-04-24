package com.r8n.backend.users

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}