# Claude (claude-sonnet-4-6) — Action Plan & Tasks

## Action Plan

Claude's scope in **skeletoni** is documentation, architecture definition, configuration files, CI/CD pipelines, and code review. Claude operates via the claude.ai chat interface and produces files that are committed to the repository by the human developer.

The overall strategy is to establish a solid, well-documented skeleton that any developer (or agent) can pick up and extend without ambiguity. Every architectural decision is made explicit in documentation before any code is written, so that code generation agents (Gemini, Copilot, etc.) have a reliable spec to work from.

### Planned Phases

1. **Documentation foundation** — README, AGENTS.md, tasks files
2. **Configuration layer** — `application.yml`, `compose.yml`, `Dockerfile`
3. **CI/CD** — GitHub Actions `ci.yml` and `release.yml`
4. **Contract layer** — `openapi.yml`, `asyncapi.yml`, `.proto` stubs
5. **Code scaffolding** — skeleton Java classes for each module (ports, commands, queries, domain model)
6. **Observability** — Grafana dashboard JSON, Prometheus config
7. **Review support** — reviewing Gemini-generated POM files and structure for correctness

---

## Tasks Done

- [x] **Drafted initial README.md (v1)**: First version covering Java 21 + Spring Boot 3.x stack with Docker, OpenAPI, Flyway, Testcontainers, Maven single-module layout.

- [x] **Rewrote README.md (v2)**: Full rewrite after receiving complete requirements. Updated to Spring Boot 4.0, added AsyncAPI 3, gRPC, Kafka, RabbitMQ, MongoDB, Couchbase, Resilience4j, MapStruct, Micrometer, Grafana, SLF4J, schedulers, SonarCloud, CQRS, event-driven architecture, and multi-module structure (contract / application / domain / infrastructure / logging / observability).

- [x] **Created AGENTS.md**: Coordination guide for all AI agents working on the repository. Includes ground rules, ownership map, tasks file format, coordination protocol, approved tech stack, module decision tree, and naming conventions.

- [x] **Created claude-tasks.md**: This file.

- [x] **Created gemini-tasks.md**: Transcribed from human-provided content. Reformatted to match the standard tasks file format defined in AGENTS.md.

---

## Tasks Pending

- [ ] **`compose.yml`**: Docker Compose file for all local dev infrastructure — PostgreSQL, MongoDB, Couchbase, Kafka + Kafka UI, RabbitMQ + Management UI, Prometheus, Grafana.

- [ ] **`application.yml`**: Full configuration file with all externalized properties for every integration (DB URLs, Kafka topics, RabbitMQ exchanges, Resilience4j settings, scheduler crons, actuator config, logging format).

- [ ] **`Dockerfile`**: Multi-stage Maven build → minimal JRE 21 runtime image.

- [ ] **`.github/workflows/ci.yml`**: Full CI pipeline — build, unit tests, integration tests (Testcontainers), SonarCloud, Docker build.

- [ ] **`.github/workflows/release.yml`**: Release pipeline — full test suite, Docker push, GitHub Release notes.

- [ ] **`contract/openapi.yml`**: OpenAPI 3 spec stub with info block, servers, and an example endpoint.

- [ ] **`contract/asyncapi.yml`**: AsyncAPI 3 spec documenting example Kafka topic and RabbitMQ exchange with payload schemas.

- [ ] **Domain skeleton classes**: `Example.java` aggregate, `ExampleId.java` value object, `ExampleCreatedEvent.java`.

- [ ] **Application skeleton classes**: `CreateExampleCommand`, `CreateExampleCommandHandler`, `GetExampleByIdQuery`, `GetExampleByIdQueryHandler`, `ExampleUseCase` port, `ExampleRepository` port, `ExampleEventPublisher` port.

- [ ] **Infrastructure skeleton classes**: `ExampleJpaEntity`, `ExampleJpaRepository`, `ExamplePostgresAdapter`, `ExampleDocument`, `ExampleMongoAdapter`, `ExampleKafkaProducer`, `ExampleKafkaConsumer`, `ExampleMapper`.

- [ ] **Grafana dashboard**: Pre-built dashboard JSON for JVM, HTTP, Kafka lag, RabbitMQ queue depth, and custom business metrics.

- [ ] **Review Gemini POMs**: Check all `pom.xml` files for correct dependency scopes, missing plugins (compiler, surefire, failsafe, sonar, protobuf), and version alignment with Spring Boot 4.0 BOM.

---

## Decisions & Notes

- **Spring Boot 4.0 chosen**: Spring Boot 4.0 targets Spring Framework 7 and requires Java 17+ minimum. Java 21 is the natural pairing for Virtual Threads (Project Loom) which are stable in this version. All dependency versions in the parent POM must be compatible with the Spring Boot 4.0 BOM.

- **AsyncAPI 3 alongside OpenAPI 3**: REST contracts are documented with OpenAPI (SpringDoc auto-generates from annotations). Messaging contracts (Kafka, RabbitMQ) are documented manually in `asyncapi.yml` — there is no mature auto-generation tool for AsyncAPI from Spring annotations yet. The `contract` module holds both specs as the single source of truth.

- **`code` wrapper module (Gemini's decision)**: Gemini introduced a `code` parent module wrapping all six functional modules. This is a valid Maven pattern for grouping source modules separately from config/tooling at the root level. Claude will respect and document this structure rather than flattening it.

- **Flyway over Liquibase**: Flyway chosen for simplicity. Plain SQL migrations are easier to read, review, and diff than XML/YAML changesets. Sufficient for a skeleton project.

- **MapStruct over ModelMapper**: MapStruct generates compile-time mappers with zero reflection overhead. Critical for performance in high-throughput microservices. ModelMapper uses runtime reflection and is not appropriate here.

- **Module boundary for mappers**: MapStruct mappers are placed in `infrastructure`, not `application` or `domain`, because mapping often requires knowledge of JPA entities or MongoDB documents which are infrastructure concerns. The `application` layer works only with domain objects and DTOs defined in `contract`.

---

## Files Created or Modified

| File | Action | Reason |
|---|---|---|
| `README.md` | Created (v1 → v2) | Project documentation — two iterations as requirements were refined |
| `AGENTS.md` | Created | AI agent coordination protocol |
| `claude-tasks.md` | Created | This action log |
| `gemini-tasks.md` | Created | Transcribed from human-provided Gemini output, reformatted to standard |