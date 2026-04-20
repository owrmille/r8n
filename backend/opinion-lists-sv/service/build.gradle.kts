plugins {
    id("convention-nonreactive-backend-service")
    id("convention-database-consumer")
}

group = "com.r8n.backend"
version = "0.0.1-SNAPSHOT"
description = "provides individual opinions"

dependencies {
    implementation(project(":opinion-lists-api"))
    implementation(project(":opinion-lists-api-integration"))
    implementation(project(":users-api-integration"))
    implementation(project(":users-client"))
    implementation(project(":mock-sv"))
    implementation(project(":opinions-sv"))
    implementation(libs.spring.kafka)
    testImplementation(project(":mock-api"))
}