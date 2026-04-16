plugins {
    id("convention-integration")
}

group = "com.r8n.backend.access"
version = "0.0.1-SNAPSHOT"
description = "access service integration package as entry point for other services"

dependencies {
    implementation(project(":core:security"))
    implementation(project(":access-api"))
    implementation(project(":access-api-integration"))
}