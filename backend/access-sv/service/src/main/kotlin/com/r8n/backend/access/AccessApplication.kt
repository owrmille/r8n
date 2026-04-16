package com.r8n.backend.access

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.r8n.backend.access", "com.r8n.backend.security"])
class AccessApplication

fun main(args: Array<String>) {
    runApplication<AccessApplication>(*args)
}