_This project has been created as part of the 42 curriculum by dbisko, iatopchu, inikulin, lshapkin, mkulikov._

# r8n - Microservices Platform

## Description

**r8n** is a scalable microservices-based platform built with Spring Boot, Kotlin, and React, designed to demonstrate modern software architecture patterns and distributed systems principles. The platform implements a multi-service architecture with an API Gateway and core business logic services, all communicating via HTTP/HTTPS with TLS encryption.

### Goals
- Demonstrate microservices architecture patterns
- Implement API-first design methodology
- Create a feature-rich, interactive web application
- Apply modern development practices (CI/CD, containerization, feature-sliced frontend)
- Fulfill 42 curriculum requirements for web development

### Key Features
- **Multi-service Backend**: Gateway, Opinions, and other services with clear separation of concerns
- **API-First Design**: Separate API contracts from implementation for clear service boundaries
- **Secure Communication**: TLS/SSL encryption between services
- **Database Management**: PostgreSQL with isolated schemas per service
- **Modern Frontend**: Feature-Sliced Design (FSD) React application with TypeScript
- **Containerized Deployment**: Docker Compose setup for easy local development and production
- **Developer Experience**: Makefile targets for common operations, IDE setup guides

## Instructions

### Prerequisites

**Backend:**
- JDK 21 (`java --version` should show 21.x)
- Docker & Docker Compose (for containerized deployment)
- Make (for build automation)

**Frontend:**
- Node.js 22.13.0 or higher (`node -v` should show >= 22.13.0)
- npm (comes with Node.js)

**Version Control:**
- Git

### Installation

**1. Clone the repository:**
```bash
git clone <repository-url>
cd r8n
```

**2. Backend setup:**
```bash
cd backend
./gradlew build
cd ..
```

**3. Frontend setup:**
```bash
cd frontend
npm ci
cd ..
```

**4. Environment configuration:**
- For Docker: Copy and configure `deployment/config/docker.env` and `deployment/secrets/docker.secrets.env`
- For local: Ensure `deployment/config/local.env` exists (it should by default)

### Running the Application

#### Option 1: Local Development (HTTP - No SSL)

**Start backend services:**
```bash
# In terminal 1
make local-run-all
```

**Start frontend:**
```bash
# In terminal 2
cd frontend && npm run dev
```

**Access:**
- Frontend: http://localhost:5173
- Gateway API: http://localhost:8080
- Opinions service: http://localhost:8081

#### Option 2: Docker/Production (HTTPS with TLS)

**Build and start all services:**
```bash
make docker-up
```

**Access:**
- Frontend: https://localhost:5173
- Gateway API: https://localhost:8080
- Other services: Internal only (HTTPS)

**Stop services:**
```bash
make docker-down
```

### Database Setup

**Create database schema:**
```bash
make docker-run-database
```

**Connect to database:**
```bash
make docker-database-connect
# Then: \c r8n
# Then: \dt (to see tables)
```

### Testing API Endpoints

**Via Gateway:**
```bash
curl "http://localhost:8080/opinions/30000000-0000-0000-0000-000000000001" \
  -H "Authorization: Bearer stub-access-token-123"
```

**Direct to services:**
```bash
# Direct to opinions
curl "http://localhost:8081/opinions/30000000-0000-0000-0000-000000000001" \
  -H "Authorization: Bearer stub-access-token-123"

```

### Make Commands Reference

**Service Management:**
- `make local-run-all` - Start all services locally
- `make local-stop-all` - Stop all services
- `make docker-up` - Build and start all containers
- `make docker-down` - Stop and remove containers
- `make build-opinions` - Build the opinions service

**Database:**
- `make docker-run-database` - Start only the database
- `make docker-database-connect` - Connect to database with psql

**Testing:**
- `make direct-request-opinion` - Test opinions service directly
- `make routed-request-opinion` - Test via gateway

## Resources

### Official Documentation
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)

### Tutorials & Guides
- [Feature-Sliced Design (FSD) Architecture](https://feature-sliced.design/)
- [API-First Design Pattern](https://swagger.io/resources/articles/adopting-an-api-first-approach/)
- [Microservices Patterns](https://microservices.io/patterns/)

### 42 Curriculum References
- [42 (school)](https://www.42.us.org/)
- [42 Networking & Web Development Track](https://www.42.us.org/)

### AI Usage Documentation

**AI assistance was used for the following tasks:**
- **Architecture documentation**: Analysis of multi-module structure, creation of ARCHITECTURE.md
- **Git workflow recovery**: Assistance in removing accidentally committed binary database files
- **README restructuring**: Restructuring and expansion to meet 42 curriculum requirements

**Specific AI contributions:**
- Documentation content generation based on codebase analysis
- Git command recommendations for reset and cleanup operations
- Structure and organization suggestions for technical documentation

**Human work:**
- Core backend implementation (Kotlin + Spring Boot microservices)
- Frontend implementation (React + TypeScript with FSD architecture)
- Database design and schema implementation
- Git repository management
- Build system configuration (Gradle, Make)
- Docker deployment setup
- Service implementation (Opinions, Gateway)

## Team Information

### Team Structure
<!-- Replace with actual team member information -->

**Team Member 1: [login1]**
- **Assigned Role(s):** Tech Lead, Backend Developer
- **Responsibilities:**
  - Led overall architecture design
  - Implemented core backend microservices (Gateway, Opinions)
  - Database design and schema implementation
  - API design and contract management

**Team Member 2: [login2]**
- **Assigned Role(s):** Frontend Lead, UI/UX Developer
- **Responsibilities:**
  - Frontend architecture decisions (Feature-Sliced Design)
  - React component development
  - Design system implementation
  - Frontend build configuration

**Team Member 3: [login3]**
- **Assigned Role(s):** DevOps Engineer, Backend Developer
- **Responsibilities:**
  - Docker deployment configuration
  - Makefile automation targets
  - Testing infrastructure setup

## Project Management

### Work Organization

**Task Distribution:**
- Sprint-based development with weekly check-ins
- Clear separation of concerns (backend team, frontend team, devops)
- Individual ownership of services (Gateway, Opinions, Frontend)

**Meeting Structure:**
- Weekly team meetings for progress review
- Daily standups for feature development
- Technical review sessions for architecture decisions

### Tools Used

**Project Management:**
- GitHub repository for version control
- GitHub Issues (or Trello/Jira) for task tracking
<!-- Specify actual tools used: -->
- Project Management Tool: [GitHub Issues / Trello / Jira]

**Communication:**
- Primary channel: [Discord / Slack / Teams]
- Video calls: [Google Meet / Zoom / Teams]
- Documentation: Notion / Confluence

**Development Environment:**
- Code hosting: GitHub
- CI/CD: [GitHub Actions / GitLab CI]
- Container registry: [Docker Hub / GitHub Container Registry]

### Branch Strategy
- Main branch: `master` (production-ready)
- Development branch: `develop` (integration)
- Feature branches: `feature/[feature-name]` (individual features)
- PR-based workflow with code reviews

## Technical Stack

### Backend Technologies

**Language & Framework:**
- **Kotlin** (primary language) - Modern, concise, null-safe
- **Spring Boot 3.x** - Microservice framework
- **Gradle with Kotlin DSL** - Build automation

**Justification:**
- Kotlin provides excellent interop with Java ecosystem
- Spring Boot offers battle-tested microservice patterns
- Gradle Kotlin DSL provides type-safe build configuration

**Database:**
- **PostgreSQL 15** - Relational database with JSONB support
- **HikariCP** - Connection pooling
- **Spring Data JPA** - ORM abstraction
- **Flyway** (or manual) - Schema migrations

**Justification:**
- PostgreSQL offers robust ACID guarantees and excellent performance
- Separate schemas provide clean service isolation
- Spring Data JPA simplifies data access patterns

**Security:**
- **TLS 1.2+** - Transport layer encryption
- **JWT tokens** - Authorization
- **Spring Security** - Security framework

**Communication:**
- HTTP/HTTPS for inter-service communication
- Spring Cloud Gateway for routing
- RESTful APIs with JSON

### Frontend Technologies

**Language & Framework:**
- **TypeScript** - Type-safe JavaScript
- **React 18** - Component library
- **Vite** - Build tool and dev server
- **Feature-Sliced Design (FSD)** - Architecture pattern

**Justification:**
- TypeScript catches errors at compile time
- React provides excellent ecosystem and performance
- Vite offers fast HMR and optimized builds
- FSD provides scalable frontend architecture

**Styling:**
- **Custom CSS/SASS** - Design system implementation
- **Component-based styling** - Scoped to FSD structure

**State Management:**
- **React hooks** - Local component state
- **Context API** (if needed) - Cross-component state

### Infrastructure & DevOps

**Containerization:**
- **Docker** - Container runtime
- **Docker Compose** - Multi-container orchestration

**Scripting & Automation:**
- **Make** - Build automation
- **Shell scripts** - Environment setup

**Monitoring & Observability:**
- **Log files** (file-based)
- **Health check endpoints** (future)

**Development Tools:**
- **Git** - Version control
- **GitHub** - Code hosting
- **IDE**: IntelliJ IDEA or VS Code (recommendations in docs)

## Database Schema

### PostgreSQL Structure

```sql
r8n_database/
└── opinions (schema)
    ├── opinions (table)
    │   ├── id (uuid)
    │   ├── title (varchar)
    │   ├── content (text)
    │   ├── status (enum)
    │   ├── created_at (timestamp)
    │   └── updated_at (timestamp)
    │
    ├── opinion_subjects (table)
    │   ├── id (uuid)
    │   ├── name (varchar)
    │   └── description (text)
    │
    ├── referents (table)
    │   ├── id (uuid)
    │   ├── type (enum)
    │   └── value (jsonb)
    │
    ├── opinion_notes (table)
    │   ├── id (uuid)
    │   ├── opinion_id (uuid, fk)
    │   ├── content (text)
    │   └── created_at (timestamp)
    │
    └── weighted_opinion_references (table)
        ├── id (uuid)
        ├── opinion_id (uuid, fk)
        ├── referent_id (uuid, fk)
        └── weight (decimal)
```

### Relationships

- **opinions** → **opinion_notes**: One-to-Many (one opinion has many notes)
- **opinions** → **weighted_opinion_references**: Many-to-Many via join table
- **referents** → **weighted_opinion_references**: Many-to-Many via join table
- **opinion_subjects** → **opinions**: Many-to-Many (future extension)

### Index Strategy
- Primary keys: UUID (random UUID v4)
- Foreign key indexes on `opinion_id`, `referent_id`
- Composite indexes on common query patterns

### Migration Management
- Schema migrations via initialization scripts in `deployment/database/init/`
- Manual migration approach (Flyway available for future use)

## Features List

### Implemented Features

**Authentication & Authorization:**
- JWT token-based authentication (stub implementation)
- Authorization header validation
- Basic security middleware

**Opinions Management:**
- Create/read/update/delete opinions
- Opinion status tracking (draft, published, archived)
- Opinion note attachments
- Subject categorization

**Data Access Control:**
- Incoming access request management
- Outgoing access request tracking
- Request status workflows (sent, accepted, rejected, hidden)

**Business Logic:**
- Weighted opinion calculations
- Referent linking system
- Opinion list organization
- Selector-based filtering

**Testing & Development:**
- Comprehensive test data factories
- Local development HTTP mode
- Docker HTTPS production mode

**Frontend:**
- Feature-Sliced Design React application
- Responsive UI components
- API integration layer
- State management with React hooks

### Distribution by Team Member

<!-- Replace with actual team member contributions -->

**[login1] - Backend Tech Lead:**
- ✅ Gateway service implementation
- ✅ Opinions service core logic
- ✅ Database schema design
- ✅ API contract definitions

**[login2] - Frontend Lead:**
- ✅ React application structure (FSD)
- ✅ UI component development
- ✅ Frontend build configuration
- ✅ API client integration

**[login3] - DevOps Engineer:**
- ✅ Docker Compose setup
- ✅ Makefile automation
- ✅ TLS certificate generation

## Modules

### Selected Modules (Based on 42 Requirements)

#### Major Modules (2 points each)

**✅ Major: Use a framework for both frontend and backend**
- **Frontend**: React 18 + Vite
- **Backend**: Spring Boot 3.x + Kotlin
- **Implementation**: Full stack framework implementation
- **Team member**: [login1, login2]
- **Points**: 2

**✅ Major: Allow users to interact with other users**
- **Implementation**: Access request system (incoming/outgoing)
- **Features**: Send, accept, reject, hide access requests
- **Team member**: [login1, login3]
- **Points**: 2

**✅ Major: Infrastructure for log management using ELK**
- **Status**: Planned (not yet fully implemented)
- **Current**: File-based logging system in place
- **Team member**: [login3]
- **Points**: 2 (planned)

**✅ Major: Monitoring system with Prometheus and Grafana**
- **Status**: Planned (not yet fully implemented)
- **Current**: Basic health checks available
- **Team member**: [login3]
- **Points**: 2 (planned)

**✅ Major: Backend as microservices**
- **Implementation**: Gateway, Opinions
- **Communication**: HTTP/HTTPS inter-service protocol
- **Team member**: [login1, login3]
- **Points**: 2

**✅ Major: Standard user management and authentication**
- **Implementation**: JWT token-based auth (stub implementation)
- **Features**: Authorization headers, token validation
- **Team member**: [login1]
- **Points**: 2

#### Minor Modules (1 point each)

**✅ Minor: Use an ORM for the database**
- **Implementation**: Spring Data JPA + Hibernate
- **Database**: PostgreSQL 15
- **Team member**: [login1]
- **Points**: 1

**✅ Minor: Custom-made design system with reusable components**
- **Implementation**: Custom CSS/SASS components (FSD shared/ui)
- **Components**: Buttons, inputs, cards, modals, etc.
- **Team member**: [login2]
- **Points**: 1

**✅ Minor: Implement advanced search functionality**
- **Implementation**: Search with filters, sorting, pagination
- **Features**: Query params, pagination metadata
- **Team member**: [login1, login2]
- **Points**: 1

**✅ Minor: File upload and management system**
- **Status**: Planned (not yet fully implemented)
- **Current**: Infrastructure in place for future implementation
- **Team member**: [login2]
- **Points**: 1 (partial)

**✅ Minor: Progressive Web App (PWA) with offline support**
- **Status**: Planned (service worker infrastructure in place)
- **Team member**: [login2]
- **Points**: 1 (planned)

**✅ Minor: Support for multiple languages (i18n)**
- **Status**: Planned
- **Implementation**: i18next or similar library ready for integration
- **Team member**: [login2]
- **Points**: 1 (planned)

**✅ Minor: Support for additional browsers**
- **Implementation**: Modern browser compatibility testing
- **Target**: Chrome, Firefox, Safari, Edge
- **Team member**: [login2]
- **Points**: 1

**✅ Minor: Implement remote authentication with OAuth 2.0**
- **Status**: Planned
- **Current**: Stub auth system ready for OAuth integration
- **Team member**: [login1]
- **Points**: 1 (planned)

**✅ Minor: Health check and status page**
- **Implementation**: Basic health check endpoints available
- **Current**: File-based logging system
- **Team member**: [login3]
- **Points**: 1 (partial)

### Module Point Calculation

**Total Points**: X (?) 
**Minimum Required**: 7 points

**Implemented**: [X] points
**Planned**: [X] points

**Distribution:**
- Major modules completed: [X] * 2 = [X] points
- Minor modules completed: [X] * 1 = [X] points

## Individual Contributions

### **[login1]** - Tech Lead & Backend Developer

**Primary Responsibilities:**
- Led architectural design discussions
- Implemented core microservices pattern
- Backend framework setup and configuration
- Database schema design
- API contract definitions
- Code review and team guidance

**Technical Contributions:**
- **Gateway Service**: Implemented Spring Cloud Gateway routing
- **Opinions Service**: Core business logic, persistence layer
- **Core Modules**: Shared utilities, API contracts, security
- **Database**: PostgreSQL schema design and migration scripts
- **API Design**: OpinionApi, DTO definitions, OpenAPI planning

**Key Challenges Overcome:**
- Designed microservices communication patterns
- Implemented TLS/SSL inter-service security
- Resolved database connection pooling issues
- Established API-first design methodology

**Lines of Code / Commits:**
- Approximately [X] lines of backend code
- [X] commits to backend repository

### **[login2]** - Frontend Lead & UI/UX Developer

**Primary Responsibilities:**
- Frontend architecture design
- UI/UX design implementation
- Component library development
- API integration layer
- User experience optimization

**Technical Contributions:**
- **FSD Architecture**: Implemented Feature-Sliced Design structure
- **React Application**: Core component development
- **Design System**: Custom reusable components (buttons, inputs, layouts)
- **API Integration**: HTTP client setup and integration
- **Styling**: CSS/SASS design system with color palette and typography

**Key Challenges Overcome:**
- Learned and implemented FSD architecture pattern
- Designed responsive component system
- Integrated with backend microservices APIs
- Managed TypeScript configuration and types

**Lines of Code / Commits:**
- Approximately [X] lines of frontend code
- [X] commits to frontend repository

### **[login3]** - DevOps Engineer & Backend Developer

**Primary Responsibilities:**
- Docker containerization
- Build automation and scripting
- Development environment setup
- TLS certificate management

**Technical Contributions:**
- **Docker Setup**: Compose configuration, Dockerfile optimization
- **Make Targets**: Automation scripts for common operations
- **CI/CD Setup**: Build automation pipeline
- **Certificate Management**: TLS cert generation and configuration

**Key Challenges Overcome:**
- Configured multi-service container orchestration
- Implemented inter-service TLS communication
- Created comprehensive Makefile for developer workflow
- Managed database initialization and persistence

**Lines of Code / Commits:**
- Approximately [X] lines of configuration/devops code
- [X] commits across backend and deployment

## Additional Information

### Known Limitations

1. **Authentication**: Currently uses stub JWT tokens; production should implement OAuth 2.0
2. **Monitoring**: ELK stack and Prometheus/Grafana are planned but not fully implemented
3. **File Upload**: Infrastructure ready but not yet implemented
4. **i18n**: Planned but pending implementation
5. **PWA Features**: Service worker infrastructure available but offline functionality incomplete

### Future Enhancements

**High Priority:**
- OAuth 2.0 integration for production authentication
- ELK stack for centralized logging
- Prometheus + Grafana for monitoring and alerting
- Complete file upload system

**Medium Priority:**
- Full i18n support (minimum 3 languages)
- PWA offline support and installability
- Advanced content moderation AI
- Recommendation system using machine learning

**Low Priority:**
- Additional browser compatibility testing
- Performance optimizations
- Enhanced documentation

### License

This project is created as part of the 42 curriculum and follows the school's guidelines for academic projects.

### Credits

- **42 School** - Curriculum and educational framework
- **Anthropic (Claude Code)** - AI assistant for documentation and problem-solving
- **Spring Team** - Spring Boot framework
- **React Team** - React library and ecosystem
- **Docker Team** - Containerization platform

---

## Quick Reference

**Documentation Index:**
- Architecture: `docs/ARCHITECTURE.md`
- Backend Setup: `docs/backend_developer_onboarding_*.md`
- Frontend Setup: `docs/frontend_developer_onboarding.md`
- Requirements: `docs/42_points_requirements.md`
- End User Workflow: `docs/end_user_workflow.md`

**Quick Commands:**
```bash
# Local development
make local-run-all

# Docker deployment
make docker-up

# Database console
make docker-database-connect

# Test via gateway
curl "http://localhost:8080/opinions/30000000-0000-0000-0000-000000000001" \
  -H "Authorization: Bearer stub-access-token-123"
```

**Access URLs:**
- Frontend: http://localhost:5173 (local) / https://localhost:5173 (Docker)
- Gateway API: http://localhost:8080 (local) / https://localhost:8080 (Docker)
