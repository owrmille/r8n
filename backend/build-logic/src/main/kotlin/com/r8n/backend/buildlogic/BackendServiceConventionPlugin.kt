package com.r8n.backend.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.apply

@Suppress("unused") // used through reflection in :build-logic:build.gradle.kts
class BackendServiceConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("convention-language")

            dependencies.apply {
                add("implementation", "org.springframework.boot:spring-boot-starter")
                add("implementation", "org.springframework.boot:spring-boot-starter-web")
                add("implementation", "org.springdoc:springdoc-openapi-starter-webflux-ui")
                add("implementation", "org.springframework.data:spring-data-commons")
                add("implementation", project(":core:security"))

                add("implementation", "org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}