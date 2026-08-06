# Contributing to skeletoni

First off, thanks for taking the time to contribute!

## Architectural Vision
**skeletoni** follows a strict multi-protocol (REST/gRPC/Messaging) CQRS Hexagonal architecture. Please review `AGENTS.md` for the core principles and dependency flow.

## Development Setup
1. Java 21+
2. Docker & Docker Compose
3. Maven 3.9+ (no wrapper is committed — install Maven locally)

Run local infrastructure:
```bash
docker compose up -d
```

Build the project:
```bash
mvn clean install -f code/pom.xml
```

## Pull Request Guidelines
- Follow the existing package structure and naming conventions.
- Every new feature must have corresponding unit tests.
- Integration tests (using Testcontainers) belong in the `infrastructure` module.
- Do not check in secrets or hardcoded passwords to `application.yml`. Use environment variables with safe defaults.
- Keep the `domain` module free of framework dependencies (including Spring).

## AI Agent Collaboration
If you are an AI agent working on this repository, you **must** log your actions in a dedicated `{MODEL-NAME}-TASKS.md` file at the root. Follow the protocol in `AGENTS.md`.

## Coding Standards
- Use MapStruct for object mapping between layers.
- Use Lombok for boilerplate reduction (`@Value`, `@Builder`, `@Slf4j`).
- Use Flyway for all database schema changes.
- Document all new API endpoints in OpenAPI (rest) or AsyncAPI (messaging).
