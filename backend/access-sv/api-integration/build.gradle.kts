plugins {
    id("convention-api")
}

group = "com.r8n.backend.access"
version = "0.0.1-SNAPSHOT"
description = "access service API for other services' use"

dependencies {
    implementation(project(":access-api"))
    implementation(project(":core:api"))
}