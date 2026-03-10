We're creating a private review platform. Focusing on security and privacy to satisfy cautious users, German legal entities, and those who can be reviewed (public persons like politicians) or those who own something that can be reviewed (cafe owners, goods and services suppliers). Keeping the codebase clean, structured and tested.

# 1. Core Principles
- **Readability first**: Readability and maintainability before performance. Use clear naming and explain complex or non-obvious logic with comments.
- **Smallest Change**: Make the minimal change that fully solves the requested problem.
- **Priority Matrix**:
    1. **Correctness**
    2. **Security & Privacy**
    3. **Maintainability & Readability**
    4. **Consistent Architecture**
    5. **Performance**
- **Preservation**: Prefer extending existing patterns and internal utilities before introducing new abstractions or dependencies. Preserve backward compatibility unless explicitly allowed.

# 2. Technology Stack
- **Backend**: Kotlin 2.2.21, JVM 21, Spring Boot 4.0.2, Spring Cloud 2025.1.1.
- **Frontend**: Node 22+, Vue 3.5+, TypeScript 5.9+, Vite 7+, Tailwind 4+.
- **Testing**: JUnit 5, Testcontainers, Mockito, Vitest (Vue), Playwright (E2E).
- **Persistence**: PostgreSQL (separate schemas per service), Liquibase.
- **Build**: Gradle (libs.versions.toml) for backend, Makefile as developer entrypoint.
- **Future-Ready**: Keep Kafka and Kubernetes integration in mind for architectural suggestions.

# 3. Architecture & Structure
## Backend: Microservice Module Pattern
Services follow a 3-module split for isolation:
- **`*-api`**: Technology-agnostic contract (DTOs & interfaces) for external consumers (frontend/API).
- **`*-client`**: Feign client and internal DTOs for consumption by other services in the system.
- **`*-sv`**: Implementation (service) module; contains business logic and persistence.
- **Gateway**: Reactive (Spring Cloud Gateway), all other data services are non-reactive.

## Backend: Service Layers
- **Controller**: `@RestController` (suffix `Controller`). DTO-based entry point.
- **Facade**: `@Service` (suffix `Facade`). Converts models to DTOs; fetches data from other services to enrich DTOs (e.g., fetching usernames).
- **Service**: `@Service` (suffix `Service`). Business logic and orchestration within the microservice.
- **Persistence**: JPA entities (`Persistence` suffix) and Repositories. Define schemas, tables, nullability, and indexes explicitly. Consider data migration implications.

# 4. Actionable Guidelines
- **MUST**:
    - Keep API contracts explicit, stable, and versionable.
    - Use DTOs intentionally; NEVER leak persistence models into external or internal APIs.
    - Handle errors intentionally; provide consistent error responses without leaking internal details.
    - Redact or avoid sensitive values in logs, metrics, and test fixtures.
    - Write tests for every non-trivial change (Unit, Integration, or Regression).
    - Suggest explicit conventions and ArchUnit tests for consistency.
    - Explicitly define transactional boundaries. Avoid N+1 queries and inefficient loading.
- **NEVER**:
    - Introduce placeholder TODOs or bypass validation for convenience.
    - Mix unrelated formatting or refactoring into functional changes.
    - Duplicate logic that should live in a shared abstraction.
- **Communication**:
    - Clarify ambiguous requirements before making structural changes.
    - State assumptions explicitly. Recommend one valid approach when multiple exist, with a brief rationale.

# 5. Security & Privacy by Design
- **Data Minimization**: Collect, store, and expose only necessary data.
- **Protect Identifiers**: Avoid exposing internal IDs or sensitive metadata in public APIs.
- **Validate Everything**: Prefer explicit, fail-fast validation over implicit trust of client-provided data.
- **Auditability**: Design for audit trails, moderation, and reversible enforcement actions.
- **Flags**: Call out features affecting privacy, legal risk, or moderation workload.

# 6. Development Workflow
- **Makefile**: Entrypoint for developers (e.g. `make docker-up`, `make build-opinions`).
- **Gradle**: Use `./gradlew test` for running backend tests.
- **Code Style**: Mirror the existing style of the file/module (indentation, imports, comments).
- **Frontend**: Extract reusable logic into composables/utilities. Separate presentation from data-fetching. Handle loading/error/empty states explicitly.