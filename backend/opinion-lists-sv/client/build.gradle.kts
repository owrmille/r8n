plugins {
    id("convention-integration")
}

group = "com.r8n.backend.opinionlists"
version = "0.0.1-SNAPSHOT"
description = "opinion lists service integration package as entry point for other services"

dependencies {
    implementation(project(":opinion-lists-api"))
    implementation(project(":opinion-lists-api-integration"))
}