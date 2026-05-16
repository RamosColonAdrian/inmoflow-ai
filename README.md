# InmoFlow AI

SaaS application for real estate agencies to manage leads, properties, conversations, appointments and AI-assisted automatic responses.

## Tech Stack

### Backend

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Flyway
- Spring Security

### Infrastructure

- Docker Compose
- PostgreSQL local database

## Project Structure

```text
inmoflow-ai/
├── backend/
├── frontend/
├── infra/
├── docs/
├── AGENTS.md
└── docker-compose.yml
```

## Run local database

```bash
docker compose up -d
```

## Run backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows:

```powershell
cd backend
mvnw.cmd spring-boot:run
```

## Health endpoint

```text
GET http://localhost:8080/api/health
```

## Current status

Implemented:

- Initial Spring Boot backend.
- PostgreSQL local database with Docker Compose.
- Flyway migrations.
- Basic Spring Security configuration.
- Public health endpoint.
- Basic agency module with create and list endpoints.

## Next steps

- Implement property module.
- Implement lead module.
- Implement conversation module.
- Add AI assistant orchestration.
- Add frontend dashboard.