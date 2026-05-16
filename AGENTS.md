# InmoFlow AI - Agent Guidelines

## Project overview

InmoFlow AI is a SaaS application for real estate agencies.

The goal is to help agencies manage:
- leads
- properties
- conversations
- appointments
- AI-assisted automatic responses

The MVP should be simple, clean and maintainable.

## Repository structure

- backend: Spring Boot 3, Java 21, Maven.
- frontend: Next.js, React, TypeScript, Tailwind CSS.
- infra: Docker Compose and infrastructure files.
- docs: architecture, prompts and technical decisions.

## Backend rules

Use Java 21 and Spring Boot 3.

The backend is a modular monolith, not microservices.

Main packages:
- config
- health
- shared
- auth
- agency
- user
- property
- lead
- conversation
- appointment
- ai
- integration

For business modules, use this structure:
- domain
- application
- infrastructure
- api

Controllers must not contain business logic.

Services/use cases should contain application logic.

Repositories should be in infrastructure.

Use DTOs for API input and output.

Use Bean Validation in request DTOs.

Use Flyway for database migrations.

Important Flyway rule:
Once a migration has been executed, do not modify it.
If a database change is needed, create a new migration with the next version.

Do not use Hibernate ddl-auto to create production tables.
Use:
spring.jpa.hibernate.ddl-auto=validate

## Database

Use PostgreSQL.

Local database runs with Docker Compose.

Main local database config:
- database: inmoflow
- user: inmoflow
- password: inmoflow
- port: 5432

## Security

Spring Security is enabled.

For now, only temporary development endpoints can be public.

Do not leave business endpoints public in final production code.

## AI rules

Do not call AI providers directly from controllers.

Use an AI application service/orchestrator.

AI-generated responses must be auditable.

The AI must not invent property information.

If confidence is low, require human approval.

Keep prompts in backend resources when possible.

## Testing rules

Use JUnit 5 and Mockito.

Add tests for new application services when practical.

When changing backend code, run:

cd backend
./mvnw test

On Windows, use:

cd backend
mvnw.cmd test

## Style

Keep code simple.

Prefer explicit names.

Avoid over-engineering.

Do not introduce microservices yet.

Do not add dependencies without a clear reason.

Explain changes after completing a task.