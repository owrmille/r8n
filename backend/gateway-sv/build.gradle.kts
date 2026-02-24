plugins {
    id("convention-language")
}

dependencies {
    implementation(libs.spring.boot.webflux)
    implementation(libs.spring.cloud.gateway)
}
