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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
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