## Architecture

Detailed architecture documentation covering microservices, API-first design, frontend structure, communication patterns, and deployment: **[Architecture Overview](docs/ARCHITECTURE.md)**

## Developer Onboarding Guides

### Backend Development
- [Backend setup with IntelliJ IDEA](docs/backend_developer_onboarding_IntelliJIDEA.md)
- [Backend setup with VS Code](docs/backend_developer_onboarding_VSCode.md)

### Frontend Development
- [Frontend developer onboarding](docs/frontend_developer_onboarding.md)

### Project Requirements
- [42 points requirements](docs/42_points_requirements.md)

## Quick Start

### Prerequisites
- JDK 21
- Node.js 22+ & npm
- Docker & Docker Compose (for containerized deployment)

### Running the Application

**Option 1: Local Development (HTTP)**
```bash
# Start all backend services
cd backend && make local-run-all

# In another terminal, start frontend
cd frontend && npm run dev

# Access at http://localhost:5173
# Gateway at http://localhost:8080
```

**Option 2: Docker (HTTPS)**
```bash
# Build and start all services
make docker-up

# Access at https://localhost:5173
# Gateway at https://localhost:8080 (HTTPS with TLS)
```

## Additional Documentation

- [End user workflow](docs/end_user_workflow.md)
