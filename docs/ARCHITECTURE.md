# Architecture Overview

This document describes the high-level architecture of the r8n application, a microservices-based platform built with Spring Boot, Kotlin, and React.

## System Overview

r8n is structured as a **microservices architecture** with 5 backend services, a PostgreSQL database, and a React frontend. All services communicate via HTTP or HTTPS depending on the environment.

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Frontend  │────────▶│   Gateway    │────────▶│   Opinions  │
│  (React)    │  HTTPS  │    -sv       │  HTTPS  │     -sv     │
│ (Port 5173) │(Docker) │ (Port 8080)  │         │ (Port 8081) │
└─────────────┘         └──────────────┘         └─────────────┘
                                 │                          │
                                 │                          │
                                 ▼                          ▼
                           ┌──────────────┐         ┌─────────────┐
                           │    Mock      │         │  PostgreSQL │
                           │     -sv      │         │ (Port 5432) │
                           │ (Port 8090)  │         └─────────────┘
                           └──────────────┘

HTTP/HTTPS Configuration:
- Local deployment: HTTP only (ports 8080, 8081, 8090)
- Docker/Production: HTTPS only (port 8080 with TLS certificates)
```

## Backend Architecture

### Tech Stack

- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 4.0.2
- **Build Tool**: Gradle 9.3.0 with Kotlin DSL
- **Database**: PostgreSQL 15
- **Security**: TLS 1.2+, JWT tokens (when SSL enabled)
- **Architecture Pattern**: API-First Design, Multi-module Microservices

### Multi-Module Structure

The backend is organized as a Gradle multi-module project with the following structure:

```
backend/
├── settings.gradle.kts          # Module definitions
├── build.gradle.kts             # Root build configuration
├── platform/                     # Platform-wide configurations
├── core/                         # Shared core modules
│   ├── api/                      # Common API contracts (pagination, etc.)
│   ├── security/                 # Security utilities
│   └── utils/                    # Common utilities
│
├── gateway-sv/                   # API Gateway Service
│   └── service/                  # Gateway implementation
│       └── src/main/kotlin/.../gateway/
│           ├── GatewayApplication.kt
│           └── config/           # Routing configuration
│
├── opinions-sv/                  # Opinions Service
│   ├── api/                      # API contracts & DTOs
│   │   └── opinions/api/         # OpinionApi, DTOs
│   └── service/                  # Business logic implementation
│       └── src/main/kotlin/.../opinions/
│           ├── OpinionsApplication.kt
│           ├── controller/       # REST controllers
│           ├── facade/           # Business logic layer
│           ├── persistence/      # Data persistence layer
│           ├── provider/         # Data providers/repositories
│           └── domain/           # Domain entities
│
└── mock-sv/                      # Mock Service
    ├── api/                      # Mock API contracts
    │   └── mock/api/             # Stub APIs
    ├── client/                   # HTTP Clients (future use)
    └── service/                  # Stub implementations
        └── src/main/kotlin/.../mock/
            ├── MockApplication.kt
            ├── controller/       # Mock controllers (Stub*)
            └── stub/             # Test data factories
```

### Microservices

#### 1. Gateway Service (`gateway-sv`)
- **Local Port**: 8080 (HTTP)
- **Docker Port**: 8080 (HTTPS with TLS)
- **Role**: API Gateway, routing, TLS termination
- **Routes requests to**: opinions-sv, mock-sv, users-sv, export-sv
- **HTTP/HTTPS**: Configured via `SERVER_SSL_ENABLED` environment variable
  - Local: `false` (HTTP only)
  - Docker: `true` (HTTPS only)

#### 2. Opinions Service (`opinions-sv`)
- **Local Port**: 8081 (HTTP)
- **Docker Port**: 8080 (HTTPS internally)
- **Role**: Core business logic for opinions
- **Database**: PostgreSQL `opinions` schema
- **Key Entities**: Opinion, OpinionSubject, Referent, OpinionNote, WeightedOpinionReference
- **HTTP/HTTPS**: Uses `INTERSERVICE_PROTOCOL` (http=local, https=Docker)
- **Packages**: `api/`, `api-integration/`, `client/` for API contracts and REST clients

#### 3. Mock Service (`mock-sv`)
- **Local Port**: 8090 (HTTP)
- **Docker Port**: 8080 (HTTPS internally)
- **Role**: Provides stub/test data for development and testing
- **Mocks**: AccessRequests, OpinionLists, Selectors, Recommendations, Messaging
- **HTTP/HTTPS**: Uses `INTERSERVICE_PROTOCOL` (http=local, https=Docker)
- **Packages**: `api/`, `client/` for API contracts and REST clients

#### 4. Users Service (`users-sv`)
- **Local Port**: 8082 (HTTP)
- **Docker Port**: 8080 (HTTPS internally)
- **Role**: User management, profiles, authentication
- **Database**: PostgreSQL `users` schema
- **Key Features**: User profiles, avatar upload, authentication endpoints
- **HTTP/HTTPS**: Uses `INTERSERVICE_PROTOCOL` (http=local, https=Docker)
- **Packages**: `api/`, `api-integration/`, `client/` for API contracts and REST clients

#### 5. Export Service (`export-sv`)
- **Local Port**: 8083 (HTTP)
- **Docker Port**: 8080 (HTTPS internally)
- **Role**: Data export functionality, async job processing
- **Database**: PostgreSQL `export` schema
- **Key Features**: Export jobs, data generation, async processing
- **HTTP/HTTPS**: Uses `INTERSERVICE_PROTOCOL` (http=local, https=Docker)
- **Packages**: `api/`, `api-integration/`, `client/` for API contracts and REST clients

### API-First Design Pattern

Each service follows an **API-first design**: the API contracts are defined separately from their implementations.

**Example for opinions-sv:**
```
opinions-sv/
├── api/ # Public API contracts (exposed to clients)
│   ├── OpinionApi.kt # Interface with @RequestMapping
│   └── dto/ # Data Transfer Objects
│       └── opinion/OpinionDto.kt
│
├── api-integration/ # Internal API contracts (for other services)
│   └── OpinionInternalApi.kt
│
├── client/ # REST clients to call other services
│   └── OpinionRestClient.kt
│
└── service/ # Implementation
    └── controller/
        └── OpinionController.kt
```

**Benefits:**
- Clear contract between services
- Can generate API documentation from interfaces
- Easy to implement mock/stub versions
- Frontend can develop against API contracts

### Design Patterns Used

1. **API-First**: Separate API contracts from implementation
2. **Facade Pattern**: Merges domain models with DTOs from other microservices and converts everything into the service's DTOs. Calls the service layer which creates domain entities, implements business logic (calculating data relying only on objects owned by the current service). The repository+persistence layer handles database interaction without modifying objects.
3. **Repository Pattern**: Data access abstraction
4. **Dependency Injection**: Via Spring Framework
5. **DTO Pattern**: Separate API models from domain entities

### Database Architecture

**PostgreSQL with Multi-Schema Design:**
```sql
r8n_db/
├── opinions (schema)
│   ├── opinions (table)
│   ├── opinion_subjects (table)
│   ├── referents (table)
│   ├── opinion_notes (table)
│   └── weighted_opinion_references (table)
└── -- future services/schemas
```

**Database Configuration:**
- Connection pooling via HikariCP
- Liquibase or manual migrations (see `deployment/database/init/`)
- Separate schemas per service for isolation
- Connection via environment variables

### Security Architecture

1. **TLS/SSL** in Docker: All inter-service communication encrypted (TLS 1.2+)
2. **HTTP** in local deployments: Plain HTTP for simplicity
3. **Authentication**: JWT Bearer tokens (stub tokens in development)
4. **Certificate Generation**: TLS certificates generated on build machine before Docker deployment

### Port & Protocol Configuration

**Environment determined:**
- **Local (make local-run-all)**: HTTP only
  ```
  INTERSERVICE_PROTOCOL=http
  SERVER_SSL_ENABLED=false
  Ports: 8080, 8081, 8082, 8083, 8090
  ```

- **Docker (make docker-up)**: HTTPS only
  ```
  INTERSERVICE_PROTOCOL=https
  SERVER_SSL_ENABLED=true
  Ports: 8080 (HTTPS), 8443 (HTTPS), database:5432
  ```

## Frontend Architecture

### Tech Stack

- **Language**: TypeScript 5.8.3
- **Framework**: Vite 8.0.1 + React 18.3.1
- **Architecture**: Feature-Sliced Design (FSD)
- **Styling**: CSS/SASS (custom design system)

### Feature-Sliced Design Structure

```
frontend/
└── src/
    ├── app/                  # Application layer
    │   └── App.tsx          # Root app component
    │
    ├── pages/                # Page components
    │   └── e.g., HomePage.tsx
    │
    ├── widgets/              # Complex reusable UI components
    │   └── e.g., HeaderWidget/
    │
    ├── features/             # Feature-specific business logic
    │   └── e.g., opinion/
    │       └── api/          # Feature API client
    │       └── model/        # Business logic
    │       └── ui/           # Feature UI components
    │
    ├── entities/             # Business entities
    │   ├── api/             # Domain API clients
    │   └── model/           # Domain models
    │
    └── shared/               # Foundation layer
        ├── ui/               # UI kit components (buttons, inputs, etc.)
        ├── api/              # Base HTTP client
        ├── lib/              # Pure utilities
        ├── types/            # Common TypeScript types
        └── styles/           # Global styles
```

## Infrastructure

### Docker Deployment

**Services (docker-compose.yml):**
1. **database** (PostgreSQL 15)
   - Port: 5432
   - Volume: postgres_data (stored in repository folder)

2. **gateway** (Spring Boot)
   - Ports: 8080 (HTTPS, externally accessible)
   - TLS certificates: /certs/keystore-gateway.p12
   - Intra-service: HTTPS to opinions/mock

3. **opinions** (Spring Boot)
   - Port: 8080 (internal HTTPS only)
   - TLS certificates: /certs/keystore-opinions.p12
   - Internal service only

4. **mock** (Spring Boot)
   - Port: 8080 (internal HTTPS only)
   - TLS certificates: /certs/keystore-mock.p12
   - Internal service only

### Docker Port Mapping Summary

```
Host            ⇄  Container
────────────────────────────────
localhost:8080  ⇄  gateway:8080 (HTTPS)
localhost:5432  ⇄  database:5432 (PostgreSQL)

Internal only (not exposed to host):
  gateway → opinions:8080 (HTTPS)
  gateway → mock:8080 (HTTPS)
```

### Local Development

**Without Docker:**
```bash
make local-run-all  # Starts gateway, opinions, mock via HTTP
make local-stop-all # Stops all services
```

**With Docker:**
```bash
make docker-up   # Build and start all containers via HTTPS
make docker-down # Stop and remove containers
```

### Environment Configuration

**Key Environment Files:**
- `deployment/config/docker.env` - Docker environment (HTTPS)
- `deployment/config/local.env` - Local development (HTTP)
- `deployment/secrets/docker.secrets.env` - Secrets (TLS passwords)

### Network Configuration

- All services connect via Docker network `r8n_net`
- Internal service resolution via service names
- Gateway routes:
  - `/api/opinions/**` → opinions-sv:8080
  - `/api/auth/**`, `/api/users/**` → users-sv:8080
- `/api/export/**` → export-sv:8080
- `/api/access-requests/**`, `/api/opinion-lists/**`, `/api/selectors/**` → mock-sv:8080

### Make Targets

**Service Management:**
- `make local-run-all` - Start all services locally (HTTP)
- `make local-stop-all` - Stop all services
- `make docker-up` - Docker build and run (HTTPS)
- `make docker-down` - Docker stop

**Testing:**
- `make direct-request-opinion` - Test opinions service directly
- `make routed-request-opinion` - Test via gateway
- `make direct-request-mock` - Test mock service directly

**Database:**
- `make docker-run-database` - Start only database
- `make docker-database-connect` - Connect to DB with psql

## Communication Patterns

### External → Gateway → Services

```
Client (Browser)
    ↓ HTTPS (Docker) or HTTP (local)
Gateway (Port 8080)
    ↓ HTTPS (Docker) or HTTP (local)
    ├──┬─→ Opinions (Port 8081 local / 8080 Docker)
    │
    └─→ Mock (Port 8090 local / 8080 Docker)
```

### Protocol Configuration

**Protocol controlled by environment variables:**
- `INTERSERVICE_PROTOCOL` - Protocol for service-to-service communication
  - Local: `http` (deployment/config/local.env)
  - Docker: `https` (deployment/config/docker.env)

- `SERVER_SSL_ENABLED` - Whether service listens via HTTPS
  - Local: `false` (HTTP)
  - Docker: `true` (HTTPS)

### Authentication Flow

1. Client includes JWT token: `Authorization: Bearer <token>`
2. Gateway validates token or passes through
3. Services accept stub tokens in development
4. Production: integrate with OAuth2/Auth0

### API Examples

For local HTTP requests, use the Makefile targets (see `make help` for full list):

**Local Development (HTTP):**
```bash
make routed-request-opinion  # Via Gateway
make routed-request-mock     # Opinions list via Gateway
```

**Docker/Production (HTTPS):**
```bash
make https-routed-request-opinion  # Via Gateway (HTTPS)
make https-routed-request-mock     # Opinions list via Gateway (HTTPS)
```
## Data Flow Example

### Request Flow: Get Opinion by ID

1. **Client** → GET `/opinions/{id}` to **Gateway**
2. **Gateway** → Routes to **Opinions Service** (TLS in Docker, plaintext in local)
3. **Opinions Controller** → Validates input, calls `OpinionFacade`
4. **OpinionFacade** → Orchestrates this service with other services (merges DTOs, converts to service DTOs), prepares response DTO
5. **Controller** → Returns data to Gateway → Client

### Mock Flow: Get Opinion List Summary

1. **Client** → GET `/opinion-lists/{id}/summary` to **Gateway**
2. **Gateway** → Routes to **Mock Service** (TLS in Docker, plaintext in local)
3. **Mock Controller** → Returns stub data from **Data Factory**
4. **Response** → Returns static/test data

## Data Flow Example (Docker/HTTPS Mode)

```
Request: https://localhost:8080/opinions/123
         │
         ▼
┌─────────────────────────────────┐
│ Gateway Service (Port 8080)    │
│ HTTPS (TLS)                     │
└─────────────────────────────────┘
         │
         │ https://opinions:8080
         │ (HTTPS, internal)
         ▼
┌─────────────────────────────────┐
│ Opinions Service (Port 8080)   │
│ HTTPS (TLS)                     │
└─────────────────────────────────┘
         │
         │ PostgreSQL (Port 5432)
         │ (plaintext localhost)
         ▼
┌─────────────────────────────────┐
│ PostgreSQL Database             │
│ opinions schema                 │
└─────────────────────────────────┘
```

## Future Architecture Extensions

Based on **42 requirements** (noted as "?" in requirements doc):

- ✅ Implemented: Microservices, ORM, standard auth, PWA support
- 🔄 Planned/TBD:
  - OAuth 2.0 integration
  - Machine learning recommendation system
  - Content moderation AI
  - ELK stack for logging
  - Prometheus + Grafana monitoring
  - Health check & status page
  - Multi-language support (i18n)
  - Playwright for cross-browser testing

## References

- **Backend Developer Onboarding** (VSCode): `docs/backend_developer_onboarding_VSCode.md`
- **Backend Developer Onboarding** (IntelliJ): `docs/backend_developer_onboarding_IntelliJIDEA.md`
- **Frontend Developer Onboarding**: `docs/frontend_developer_onboarding.md`
- **42 Requirements**: `docs/42_points_requirements.md`
- **Docker Compose**: `docker-compose.yml`
- **Service List**: `deployment/config/services.list`
