plugins {
    id("convention-language")
}

dependencies {
    implementation(platform(project(":platform")))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.cloud.gateway)
}
