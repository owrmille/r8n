package com.r8n.backend.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import kotlin.apply

@Suppress("unused") // used through reflection in :build-logic:build.gradle.kts
class BackendServiceConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            val libs = extensions
                .getByType<VersionCatalogsExtension>()
                .named("libs")

            pluginManager.apply("convention-language")
            pluginManager.apply(libs.findPlugin("spring-boot").get().get().pluginId)

            dependencies.apply {
                add("implementation", project(":core:security"))

                add("implementation", libs.findLibrary("postgresql").get())
                add("implementation", libs.findLibrary("spring-boot-data-jpa").get())
                add("implementation", libs.findLibrary("spring-boot-starter").get())
                add("testImplementation", libs.findLibrary("spring-boot-starter-test").get())
                add("implementation", libs.findLibrary("spring-swagger").get())
            }
        }
    }
}