plugins {
    id("convention-nonreactive-backend-service")
    id("convention-database-consumer")
}

group = "com.r8n.backend.access"
version = "0.0.1-SNAPSHOT"
description = "access service for figuring out which user has access to which resources"

dependencies {
    implementation(project(":access-api"))
    implementation(project(":access-api-integration"))
    implementation(project(":mock-api"))
    implementation(project(":mock-client"))
    implementation(project(":users-api-integration"))
    implementation(project(":users-client"))
    implementation(project(":opinions-api"))
    implementation(project(":core:security"))
    implementation(project(":core:api"))
    implementation(project(":core:utils"))
    
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("com.h2database:h2")
}