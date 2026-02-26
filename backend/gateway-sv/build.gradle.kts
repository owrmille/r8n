plugins {
    id("convention-language")
}

dependencies {
    implementation(platform(project(":platform")))
    implementation(libs.spring.boot.webflux)
    implementation(libs.spring.cloud.gateway)
}
