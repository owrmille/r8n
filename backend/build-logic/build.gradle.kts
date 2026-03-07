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
    implementation(libs.kotlin.gradle)
    implementation(libs.spring.boot.gradle)
    implementation(libs.kotlin.jpa)
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
        create("convention-database-consumer") {
            id = "convention-database-consumer"
            implementationClass = "com.r8n.backend.buildlogic.DatabaseConsumerConventionPlugin"
        }
    }
}