plugins {
    id("convention-api")
}

group = "com.r8n.backend.access"
version = "0.0.1-SNAPSHOT"
description = "access service API"
dependencies {
    implementation(project(":core:api"))
}