plugins {
    id("convention-backend-service")
}

group = "com.r8n.backend"
version = "0.0.1-SNAPSHOT"
description = "provides individual opinions"

dependencies {
    implementation(project(":opinions-api"))
    implementation(project(":mock-client"))
}