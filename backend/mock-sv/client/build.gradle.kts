plugins {
    id("convention-integration")
}

group = "com.r8n.backend.mock"
version = "0.0.1-SNAPSHOT"
description = "mock service integration package as entry point for other services"

dependencies {
    implementation(project(":mock-api"))
}