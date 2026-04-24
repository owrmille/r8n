_This project has been created as part of the 42 curriculum by dbisko, iatopchu, inikulin, lshapkin, mkulikov._

# r8n - Microservices Platform

## Description

**You don't need everyone's opinion. You need the right people's opinion.**

**r8n** platform lets you build a personal network of trusted reviewers — friends, food critics you follow, colleagues with similar taste — and see only their honest takes on restaurants and cafés. Reviews are private by design, shared only on request, which means they're protected under German law and can never be forced down.

**r8n** is a scalable microservices-based platform built with Spring Boot, Kotlin, and React, designed to demonstrate modern software architecture patterns and distributed systems principles. The platform implements a multi-service architecture with an API Gateway and core business logic services, all communicating via HTTP/HTTPS with TLS encryption.

### Goals
- Demonstrate microservices architecture patterns
- Implement API-first design methodology
- Create a feature-rich, interactive web application
- Apply modern development practices (CI/CD, containerization, feature-sliced frontend)
- Fulfill 42 curriculum requirements for web development

### Key Features
- **Multi-service Backend**: Gateway, Opinions, Users, Export, and Mock services with clear separation of concerns
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
make docker-database-run
```

**Connect to database:**
```bash
make docker-database-connect
# Then: \c r8n
# Then: \dt (to see tables)
```

### Testing API Endpoints

Use the make rules to test API endpoints (documented in Make Commands Reference below).

### Make Commands Reference

**Service Management:**
- `make local-run-all` - Start all services locally
- `make local-stop-all` - Stop all services
- `make docker-up` - Build and start all containers
- `make docker-down` - Stop and remove containers
- `make build-opinions` - Build the opinions service

**Database:**
- `make docker-database-run` - Start only the database
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

**Backend Team:**
- **inikulin** - Tech Lead, Backend Developer
  - Led architecture design, implemented Gateway and Opinions services
  - Database design, API contracts
- **iatopchu** - Backend Developer
  - Backend service development, business logic implementation
- **lshapkin** - Backend Developer
  - Backend service development, testing

**Frontend Team:**
- **mkulikov** - Frontend Lead, UI/UX Developer
  - Frontend architecture (FSD), React development, design system
- **dbisko** - Frontend Developer
  - React component development, API integration

## Project Management

### Work Organization

**Task Distribution:**
- Clear separation of concerns (backend team, frontend team, devops)
- Individual ownership of services (Gateway, Opinions, Frontend)

**Meeting Structure:**
- Weekly team meetings for progress review
- Technical review sessions for architecture decisions

### Tools Used

**Project Management:**
- GitHub repository for version control
- GitHub Issues for task tracking
- Project Management Tool: GitHub Issues

**Communication:**
- Primary channel: Slack
- Video calls: Google Meet
- Documentation: Notion

**Development Environment:**
- Code hosting: GitHub
- CI/CD: GitHub Actions
- Container registry: Local Registry

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
- **Liquibase** - Schema migrations

**Justification:**
- PostgreSQL offers robust ACID (Atomicity, Consistency, Isolation, Durability) guarantees and excellent performance
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
- Primary keys: UUID (time-ordered UUID v7)
- Foreign key indexes on `opinion_id`, `referent_id`
- Composite indexes on common query patterns

### Migration Management
- Schema migrations via initialization scripts in `deployment/database/init/`
- Manual migration approach (Liquibase available for future use)

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
- Selector-based overlay display

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

**Backend Team:**
- ✅ inikulin: Gateway service implementation, Opinions service core logic, Database schema design, API contract definitions
- ✅ iatopchu: Backend service development, business logic
- ✅ lshapkin: Backend service development, testing

**Frontend Team:**
- ✅ mkulikov: React application structure (FSD), UI component development, Frontend build configuration, API client integration
- ✅ dbisko: React component development, API integration

## Modules

### Selected Modules (Based on 42 Requirements)

#### Major Modules (2 points each)

**1. Use a framework for both the frontend and backend**

- **Points**: 2 points (Major module)
- **Description**: Use a frontend framework (React, Vue, Angular, Svelte, etc.) and a backend framework (Express, NestJS, Django, Flask, Ruby on Rails, etc.)
- **Justification**: Frameworks provide structured, maintainable codebases with built-in best practices, reducing development time and ensuring consistency across the project. Using established frameworks allows the team to focus on business logic rather than reinventing the wheel.
- **Implementation**:
  - **Frontend**: React 18 with TypeScript, Vite as build tool, and Feature-Sliced Design (FSD) architecture pattern
  - **Backend**: Spring Boot 3.x with Kotlin, using Spring Cloud Gateway for API routing and Spring Data JPA for database access
  - Both frameworks chosen for their strong ecosystems, type safety, and industry adoption
- **Team Members**: All team members (dbisko, iatopchu, inikulin, lshapkin, mkulikov)

**2. Allow users to interact with other users**

- **Points**: 2 points (Major module)
- **Description**: A basic chat system (send/receive messages between users), a profile system (view user information), and a friends system (add/remove friends, see friends list)
- **Justification**: User interaction features are essential for building a social platform where users can connect, share opinions, and build trusted networks. These features enable the core value proposition of r8n - connecting people with trusted reviewers.
- **Implementation**:
  - **Profile System**: User profiles with personal information, opinion history, and network connections
  - **Friends System**: Add/remove friends functionality, friends list display, and network visualization
  - **Chat System**: Basic messaging capabilities for direct communication between users
  - **User Management**: User service handles authentication, profile data, and relationship management
  - **Frontend Integration**: React components for profile views, friend management, and messaging interfaces
- **Team Members**: mkulikov, iatopchu

**3. A public API to interact with the database with a secured API key, rate limiting, documentation, and at least 5 endpoints**

- **Points**: 2 points (Major module)
- **Description**: Public API with secured API key authentication, rate limiting, comprehensive documentation, and at least 5 endpoints including GET, POST, PUT, DELETE operations
- **Justification**: A well-designed public API enables external integrations, third-party developers, and programmatic access to the platform's data. Security measures (API keys, rate limiting) protect against abuse while documentation ensures ease of use.
- **Implementation**:
  - **API Gateway**: Spring Cloud Gateway for centralized API management and routing
  - **Security**: API key-based authentication with Spring Security
  - **Rate Limiting**: Request rate limiting to prevent API abuse
  - **Documentation**: OpenAPI/Swagger documentation for all endpoints
  - **Endpoints**: 5+ RESTful endpoints covering CRUD operations:
    - GET /api/opinions - Retrieve opinions
    - POST /api/opinions - Create new opinions
    - PUT /api/opinions/{id} - Update opinions
    - DELETE /api/opinions/{id} - Delete opinions
    - GET /api/users/{id} - Retrieve user profiles
- **Team Members**: inikulin

**4. Standard user management and authentication**

- **Points**: 2 points (Major module)
- **Description**: Users can update their profile information, upload an avatar (with a default avatar if none provided), add other users as friends and see their online status, and have a profile page displaying their information
- **Justification**: Comprehensive user management and authentication are fundamental to any social platform. These features enable user identity, personalization, social connections, and engagement - all core to the r8n platform's value proposition of building trusted reviewer networks.
- **Implementation**:
  - **Profile Management**:
    - User profile creation and editing
    - Personal information updates (name, bio, location, interests)
    - Profile visibility settings
    - Profile completion tracking
  - **Avatar System**:
    - Avatar upload functionality with image validation
    - Default avatar generation for users without custom avatars
    - Avatar resizing and optimization
    - Avatar display across the platform
  - **Friends System**:
    - Add/remove friends functionality
    - Friend request management (send, accept, reject)
    - Friends list display with search and filtering
    - Friend relationship status tracking
  - **Online Status**:
    - Real-time online status tracking
    - Last seen timestamps
    - Status indicators (online, offline, away)
    - Presence notifications for friends
  - **Profile Pages**:
    - Public profile pages with user information
    - Opinion history and activity feed
    - Friend network visualization
    - Profile customization options
  - **Backend**: User service with Spring Security, profile data management, relationship tracking
  - **Frontend**: React profile components, avatar upload interface, friends management UI
- **Team Members**: inikulin, mkulikov

**5. Advanced permissions system**

- **Points**: 2 points (Major module)
- **Description**: View, edit, and delete users (CRUD), roles management (admin, user, guest, moderator, etc.), and different views and actions based on user role
- **Justification**: An advanced permissions system is crucial for platform security, content moderation, and administrative control. Role-based access control (RBAC) ensures users can only access features and data appropriate to their role, protecting both the platform and user data.
- **Implementation**:
  - **User CRUD Operations**:
    - View user list with filtering and search
    - Edit user information and settings
    - Delete users with proper confirmation and data cleanup
    - User status management (active, suspended, banned)
  - **Roles Management**:
    - Role definitions: Admin, Moderator, User, Guest
    - Role assignment and modification
    - Role hierarchy and inheritance
    - Custom role creation for future extensibility
  - **Permission System**:
    - Granular permissions for each role
    - Permission groups for easier management
    - Dynamic permission checking
    - Permission inheritance and overrides
  - **Role-Based Views**:
    - Admin dashboard with full system control
    - Moderator interface for content moderation
    - Standard user view with basic features
    - Guest view with limited functionality
  - **Access Control**:
    - Method-level security with Spring Security annotations
    - URL-based access control
    - Resource-level permissions
    - API endpoint protection
  - **Audit Logging**:
    - Track all permission changes
    - Log administrative actions
    - User activity monitoring
  - **Backend**: Spring Security with custom UserDetails, Role-based access control, Permission service
  - **Frontend**: Role-aware React components, conditional rendering based on permissions, Admin dashboard
- **Team Members**: inikulin

**6. Backend as microservices**

- **Points**: 2 points (Major module)
- **Description**: Design loosely-coupled services with clear interfaces, use REST APIs or message queues for communication, and ensure each service has a single responsibility
- **Justification**: Microservices architecture enables scalability, maintainability, and independent deployment of different parts of the system. Loosely-coupled services with clear boundaries allow teams to work independently, choose appropriate technologies for each service, and scale components based on demand.
- **Implementation**:
  - **Service Architecture**:
    - Gateway Service: API routing, load balancing, authentication
    - Opinions Service: Core business logic for opinions management
    - Users Service: User management, authentication, profiles
    - Export Service: Data export and reporting functionality
    - Mock Service: Testing and development support
  - **Loose Coupling**:
    - Clear service boundaries with well-defined interfaces
    - API contracts using OpenAPI specifications
    - DTOs for data transfer between services
    - Event-driven communication for async operations
  - **Communication**:
    - REST APIs for synchronous communication
    - HTTP/HTTPS with TLS encryption
    - JSON for data serialization
    - Future: Message queues for async events
  - **Single Responsibility**:
    - Each service handles one business domain
    - Separate databases per service
    - Independent deployment and scaling
    - Service-specific business logic
  - **Service Discovery**:
    - Service registration and discovery
    - Health checks and monitoring
    - Load balancing across service instances
  - **Infrastructure**:
    - Docker containerization for each service
    - Docker Compose for orchestration
    - Separate configuration per service
    - Service-specific logging and monitoring
  - **Development**:
    - Independent service development
    - Service-specific testing strategies
    - API versioning for backward compatibility
    - Gradle multi-module project structure
- **Team Members**: inikulin, iatopchu, lshapkin



#### Minor Modules (1 point each)

**1. Use a frontend framework (React, Vue, Angular, Svelte, etc.)**

- **Points**: 1 point (Minor module)
- **Description**: Use a frontend framework (React, Vue, Angular, Svelte, etc.)
- **Justification**: Frontend frameworks provide structured, component-based architecture that improves code maintainability, reusability, and developer experience. React was chosen for its large ecosystem, strong TypeScript support, and industry adoption.
- **Implementation**:
  - React 18 with TypeScript for type safety
  - Vite as build tool for fast development and optimized production builds
  - Feature-Sliced Design (FSD) architecture for scalable frontend structure
  - Component-based UI with custom design system
- **Team Members**: mkulikov, dbisko

**2. Use a backend framework (Express, Fastify, NestJS, Django, etc.)**

- **Points**: 1 point (Minor module)
- **Description**: Use a backend framework (Express, Fastify, NestJS, Django, etc.)
- **Justification**: Backend frameworks provide robust foundations for building scalable, maintainable server-side applications with built-in patterns for routing, middleware, and database integration. Spring Boot was chosen for its comprehensive ecosystem, strong Kotlin support, and production-ready features.
- **Implementation**:
  - Spring Boot 3.x with Kotlin for backend services
  - Spring Cloud Gateway for API routing and load balancing
  - Spring Data JPA for database abstraction and ORM
  - Gradle with Kotlin DSL for build automation
  - Microservices architecture with separate services (Gateway, Opinions, Users, Export)
- **Team Members**: iatopchu, inikulin

**3. Use an ORM for the database**

- **Points**: 1 point (Minor module)
- **Description**: Use an Object-Relational Mapping (ORM) tool for database operations
- **Justification**: ORMs provide abstraction layers that simplify database interactions, improve code maintainability, and reduce SQL injection risks. They enable developers to work with objects instead of raw SQL while maintaining performance through optimized queries.
- **Implementation**:
  - **Spring Data JPA**: Primary ORM framework for database operations
  - **Hibernate**: JPA implementation providing object-relational mapping
  - **Entity Mapping**: Kotlin data classes mapped to database tables
  - **Repository Pattern**: Spring Data repositories for CRUD operations
  - **Query Methods**: Derived query methods and custom JPQL queries
  - **Schema Management**: Automatic schema generation and migration support
- **Team Members**: iatopchu, inikulin

**4. Custom-made design system with reusable components, including a proper color palette, typography, and icons (minimum: 10 reusable components)**

- **Points**: 1 point (Minor module)
- **Description**: Custom-made design system with reusable components, including a proper color palette, typography, and icons (minimum: 10 reusable components)
- **Justification**: A custom design system ensures visual consistency across the application, improves development efficiency through reusable components, and creates a cohesive user experience. It establishes design standards that scale with the project.
- **Implementation**:
  - **Color Palette**: Comprehensive color system with primary, secondary, accent, and neutral colors
  - **Typography**: Consistent font family, sizes, weights, and line heights for headings and body text
  - **Icon System**: Custom icon set using SVG icons for scalability and performance
  - **Reusable Components** (10+):
    - Button components (primary, secondary, outline variants)
    - Input components (text, email, password with validation)
    - Card components for content display
    - Modal/Dialog components
    - Navigation components (header, sidebar, breadcrumbs)
    - Form components with validation
    - Badge/Tag components
    - Avatar components
    - Loading/Spinner components
    - Alert/Notification components
  - **Design Tokens**: Centralized design variables for consistency
  - **Component Documentation**: Usage guidelines and examples
- **Team Members**: mkulikov, dbisko

**5. Implement advanced search functionality with filters, sorting, and pagination**

- **Points**: 1 point (Minor module)
- **Description**: Implement advanced search functionality with filters, sorting, and pagination
- **Justification**: Advanced search capabilities are essential for users to efficiently find relevant content in large datasets. Filters, sorting, and pagination improve user experience by allowing precise control over search results and manageable data presentation.
- **Implementation**:
  - **Search Functionality**: Full-text search across opinions, users, and subjects
  - **Filter System**: Multiple filter criteria including:
    - Content type filters (opinions, users, subjects)
    - Date range filters
    - Status filters (draft, published, archived)
    - Category filters
    - User relationship filters (friends, network)
  - **Sorting Options**: Multiple sort orders including:
    - Relevance score
    - Date (newest/oldest)
    - Popularity (most viewed/most referenced)
    - Rating/quality score
  - **Pagination**: Efficient pagination with:
    - Configurable page sizes
    - Page navigation controls
    - Total result count display
    - Cursor-based pagination for large datasets
  - **Backend Implementation**: Spring Data JPA specifications for complex queries
  - **Frontend Integration**: React components for search interface, filter controls, and result display
  - **Performance Optimization**: Database indexing for search fields, query optimization
- **Team Members**: inikulin

**6. File upload and management system**

- **Points**: 1 point (Minor module)
- **Description**: Support multiple file types (images, documents, etc.), client-side and server-side validation (type, size, format), secure file storage with proper access control, file preview functionality where applicable, progress indicators for uploads, and ability to delete uploaded files
- **Justification**: File upload capabilities are essential for rich content sharing, document management, and user-generated content. A robust file management system ensures security, performance, and good user experience for file operations.
- **Implementation**:
  - **Multiple File Types**: Support for images (JPEG, PNG, GIF), documents (PDF, DOCX), and other common formats
  - **Client-side Validation**: 
    - File type validation before upload
    - File size limits with user feedback
    - Format validation using file signatures
  - **Server-side Validation**:
    - Comprehensive security checks on uploaded files
    - Virus scanning integration
    - File type verification using MIME types
    - Size and dimension validation
  - **Secure File Storage**:
    - Encrypted storage for sensitive files
    - Access control based on user permissions
    - File ownership and sharing permissions
    - Secure file URLs with expiration tokens
  - **File Preview Functionality**:
    - Image thumbnails and previews
    - Document preview for supported formats
    - Video/audio preview capabilities
  - **Progress Indicators**:
    - Real-time upload progress bars
    - Upload speed and time remaining estimates
    - Batch upload progress tracking
  - **File Management**:
    - Delete functionality with confirmation
    - File organization and folder structure
    - File metadata and versioning
  - **Backend**: Spring Boot multipart file handling, storage service integration
  - **Frontend**: React upload components with drag-and-drop support
- **Team Members**: mkulikov

**7. Support for additional browsers**

- **Points**: 1 point (Minor module)
- **Description**: Full compatibility with at least 2 additional browsers (Firefox, Safari, Edge, etc.), test and fix all features in each browser, document any browser-specific limitations, and ensure consistent UI/UX across all supported browsers
- **Justification**: Cross-browser compatibility ensures the application is accessible to all users regardless of their preferred browser. Testing across multiple browsers identifies and fixes compatibility issues, providing a consistent experience for all users.
- **Implementation**:
  - **Browser Support**: Full compatibility with Chrome (primary), Firefox, Safari, and Edge
  - **Testing Strategy**:
    - Comprehensive testing across all supported browsers
    - Automated cross-browser testing with tools like Selenium or Playwright
    - Manual testing for UI/UX consistency
    - Feature-by-feature validation in each browser
  - **Compatibility Fixes**:
    - CSS vendor prefixes and fallbacks
    - JavaScript polyfills for older browser versions
    - Responsive design testing across different browser viewports
    - API compatibility handling
  - **Documentation**:
    - Browser-specific limitations documented
    - Minimum browser version requirements specified
    - Known issues and workarounds listed
    - Feature compatibility matrix maintained
  - **Consistent UI/UX**:
    - Design system tested across all browsers
    - Component rendering consistency verified
    - Performance optimization for each browser
    - Accessibility features validated across browsers
  - **Development Tools**: Browser-specific debugging tools and dev environments
  - **Continuous Testing**: Integration with CI/CD for automated cross-browser testing
- **Team Members**: mkulikov, dbisko

**8. GDPR compliance features**

- **Points**: 1 point (Minor module)
- **Description**: Allow users to request their data, data deletion with confirmation, export user data in a readable format, and confirmation emails for data operations
- **Justification**: GDPR compliance is essential for any platform handling user data, especially in European markets. These features demonstrate commitment to data protection, user privacy rights, and legal compliance, building trust with users and avoiding regulatory penalties.
- **Implementation**:
  - **Data Request System**:
    - User data request interface
    - Data collection from all services
    - Data compilation and formatting
    - Request status tracking
  - **Data Deletion**:
    - Account deletion functionality
    - Multi-step confirmation process
    - Data removal from all services
    - Anonymous data retention where required
    - Deletion confirmation and verification
  - **Data Export**:
    - Export user data in readable formats (JSON, CSV, PDF)
    - Comprehensive data export including profiles, opinions, relationships
    - Structured data organization
    - Download functionality with expiration
  - **Confirmation Emails**:
    - Email notifications for data requests
    - Confirmation emails for data deletion
    - Export completion notifications
    - Data operation history emails
  - **Compliance Features**:
    - Data processing records
    - Consent management
    - Data retention policies
    - Right to be forgotten implementation
  - **Security**:
    - Secure data handling and storage
    - Encrypted data exports
    - Access logging for data operations
    - Verification of data ownership
  - **Backend**: Export service for data compilation, User service for data management, Email service for notifications
  - **Frontend**: React components for data requests, deletion confirmation, export interface
- **Team Members**: lshapkin, inikulin



### Module Point Calculation

**Total Points**: 20 points (6 Major modules, 8 Minor modules)
**Minimum Required**: 14 points

**Implemented**: 20 points ✅ (Exceeds minimum requirement by 6 points)
**Planned**: Additional modules for enhanced functionality

**Distribution:**
- Major modules completed: 6 * 2 = 12 points
- Minor modules completed: 8 * 1 = 8 points

## Individual Contributions

### **inikulin** - Tech Lead & Backend Developer

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
- **Core Modules**: Shared utilities, security
- **Database**: PostgreSQL schema design and migration scripts
- **API Design**: OpinionApi, DTO definitions, OpenAPI planning

**Key Challenges Overcome:**
- Designed microservices communication patterns
- Implemented TLS/SSL inter-service security
- Resolved database connection pooling issues
- Established API-first design methodology

### **iatopchu** - Backend Developer

**Primary Responsibilities:**
- Backend service development
- Business logic implementation
- Testing and quality assurance

**Technical Contributions:**
- **Opinions Service**: Business logic development
- **Backend Features**: Service implementation and refinement
- **Testing**: Unit and integration tests

### **lshapkin** - Backend Developer

**Primary Responsibilities:**
- Backend service development
- Testing infrastructure

**Technical Contributions:**
- **Service Development**: Backend logic implementation
- **Testing**: Test coverage and QA processes

### **mkulikov** - Frontend Lead & UI/UX Developer

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

### **dbisko** - Frontend Developer

**Primary Responsibilities:**
- React component development
- API integration
- Frontend testing

**Technical Contributions:**
- **React Development**: Component implementation
- **API Integration**: Frontend-backend integration
- **UI Polish**: User interface improvements

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

**Access URLs:**
- Frontend: http://localhost:5173 (local) / https://localhost:5173 (Docker)
- Gateway API: http://localhost:8080 (local) / https://localhost:8080 (Docker)
