rootProject.name = "r8n-backend"

includeBuild("build-logic")
include("platform")
include("core")

include("gateway-sv")

include("mock-api", "mock-sv/api")
include("mock-client", "mock-sv/client")
include("mock-sv", "mock-sv/service")

include("opinions-api", "opinions-sv/api")
include("opinions-sv", "opinions-sv/service")

include("core:security")
include("core:api")
include("core:utils")

fun include(name: String, projectDir: String) {
    include(name)
    project(":$name").projectDir = file(projectDir)
}