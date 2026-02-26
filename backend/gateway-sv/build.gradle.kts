plugins {
    id("convention-language")
}

dependencies {
    implementation(platform(project(":platform")))
    implementation(libs.spring.boot.web)
    implementation(libs.spring.cloud.gateway)
}
