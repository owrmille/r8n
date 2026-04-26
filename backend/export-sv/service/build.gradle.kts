plugins {
    id("convention-nonreactive-backend-service")
}

group = "com.r8n.backend"
version = "0.0.1-SNAPSHOT"
description = "provides delayed data export functionality"

dependencies {
    implementation(project(":export-api"))
    implementation(project(":users-api-integration"))
    implementation(project(":users-client"))
    implementation(project(":opinions-api"))
    implementation(project(":opinions-api-integration"))
    implementation(project(":opinions-client"))
    implementation(project(":mock-api-integration"))
    implementation(project(":mock-client"))
    implementation(project(":mock-sv"))
    testImplementation(project(":core:security"))
}
