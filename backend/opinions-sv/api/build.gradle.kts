plugins {
    id("convention-api")
}

group = "com.r8n.backend.opinions"
version = "0.0.1-SNAPSHOT"
description = "opinions service API"

dependencies {
    implementation(libs.jakarta.validation)
    implementation(libs.spring.swagger.annotations.jakarta)
}
