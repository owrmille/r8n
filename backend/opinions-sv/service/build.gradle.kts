plugins {
    id("convention-backend-service")
}

group = "com.r8n.backend"
version = "0.0.1-SNAPSHOT"
description = "provides individual opinions"

dependencies {
    implementation(project(":opinions-api"))
    implementation(project(":mock-client"))
    testImplementation(libs.mockito)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.test.autoconfigure)
}