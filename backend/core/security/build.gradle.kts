plugins {
    id("convention-language")
}

group = "com.r8n.backend"
version = "0.0.1-SNAPSHOT"
description = "security bean for backend services import"

dependencies {
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.web)
}