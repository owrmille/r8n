plugins {
    id("convention-backend-service")
}

dependencies {
    implementation(platform(project(":platform")))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.cloud.gateway)
}