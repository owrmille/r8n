plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.0.2")
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin:2.2.21")
}

gradlePlugin {
    plugins {
        create("convention-language") {
            id = "convention-language"
            implementationClass = "com.r8n.backend.buildlogic.LanguageConventionPlugin"
        }
        create("convention-backend-service") {
            id = "convention-backend-service"
            implementationClass = "com.r8n.backend.buildlogic.BackendServiceConventionPlugin"
        }
    }
}