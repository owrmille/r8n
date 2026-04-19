rootProject.name = "r8n-backend"

includeBuild("build-logic")
include("platform")
include("core")

include("gateway-sv", "gateway-sv/service")

include("mock-api", "mock-sv/api")
include("mock-api-integration", "mock-sv/api-integration")
include("mock-client", "mock-sv/client")
include("mock-sv", "mock-sv/service")

include("access-api", "access-sv/api")
include("access-api-integration", "access-sv/api-integration")
include("access-client", "access-sv/client")
include("access-sv", "access-sv/service")

include("users-api", "users-sv/api")
include("users-api-integration", "users-sv/api-integration")
include("users-client", "users-sv/client")
include("users-sv", "users-sv/service")

include("opinion-lists-api", "opinion-lists-sv/api")
include("opinion-lists-api-integration", "opinion-lists-sv/api-integration")
include("opinion-lists-client", "opinion-lists-sv/client")
include("opinion-lists-sv", "opinion-lists-sv/service")

include("opinions-api", "opinions-sv/api")
include("opinions-api-integration", "opinions-sv/api-integration")
include("opinions-client", "opinions-sv/client")
include("opinions-sv", "opinions-sv/service")

include("core:security")
include("core:api")
include("core:utils")

fun include(name: String, projectDir: String) {
    include(name)
    project(":$name").projectDir = file(projectDir)
}