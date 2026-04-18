package com.r8n.backend.access

import com.r8n.backend.opinions.integration.configuration.OpinionsRestClientConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@SpringBootApplication
@ComponentScan(basePackages = ["com.r8n.backend.access", "com.r8n.backend.security"])
@Import(OpinionsRestClientConfiguration::class)
class AccessApplication

fun main(args: Array<String>) {
    runApplication<AccessApplication>(*args)
}