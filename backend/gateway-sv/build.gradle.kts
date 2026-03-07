plugins {
    id("convention-language")
}

pluginManager.apply(libs.findPlugin("spring-boot").get().get().pluginId)

dependencies {
    implementation(platform(project(":platform")))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.cloud.gateway)
}
