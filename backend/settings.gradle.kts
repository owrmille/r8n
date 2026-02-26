plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "r8n-backend"

includeBuild("build-logic")
include("platform")
include("core")

include("gateway-sv")
include("mock-sv")
include("opinions-sv")

include("core:security")