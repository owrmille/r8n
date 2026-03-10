pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.2.21"
        id("org.jetbrains.kotlin.plugin.spring") version "2.2.21"
        id("org.jetbrains.kotlin.plugin.jpa") version "2.2.21"
        id("org.springframework.boot") version "4.0.2"
        id("io.spring.dependency-management") version "1.1.7"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
