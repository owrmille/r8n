plugins {
    id("convention-language")
}

group = "com.r8n.backend.opinions"
version = "0.0.1-SNAPSHOT"
description = "mock service integration package as entry point for other services"

dependencies {
    implementation(project(":mock-api"))
}