plugins {
    id("convention-api")
}

group = "com.r8n.backend.opinionlists"
version = "0.0.1-SNAPSHOT"
description = "opinion lists service API"

dependencies {
    implementation(project(":opinions-api"))
}