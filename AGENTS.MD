# 🤖 AGENTS.md

> This file describes how AI agents (Claude, Gemini, Copilot, Cursor, etc.) should behave when contributing to **skeletoni**.  
> Every agent that performs work on this repository **must** log its actions in a dedicated `{MODEL-NAME}-TASKS.md` file at the root of the project.

---

## Purpose

**skeletoni** is a collaborative project where multiple AI models and human developers contribute side by side. This file establishes the rules, boundaries, conventions, and coordination protocol so that agents don't conflict with each other's work, don't break the architecture, and leave a clear audit trail of every decision made.

---

## Active Agents

| Agent | Interface | Tasks File | Primary Responsibility |
|---|---|---|---|
| Gemini | JetBrains IDE plugin | `gemini-tasks.md` | Project scaffolding, Maven multi-module structure, POM configuration |
| Claude | claude.ai chat | `claude-tasks.md` | Documentation, architecture decisions, config files, CI/CD, code review |
| Human | IDE / Git | — | Final approval, domain logic, business rules |

---

## Repository Structure Primer

Before making any changes, every agent must understand the module layout:

```
skeletoni/
├── code/                          # Parent container for all functional source modules
│   ├── contract/                  # API contracts: OpenAPI, AsyncAPI, gRPC protos, DTOs
│   ├── application/               # Use cases, CQRS commands/queries, schedulers, ports
│   ├── domain/                    # Pure business logic — aggregates, events, domain services
│   ├── infrastructure/            # All I/O adapters: DBs, Kafka, RabbitMQ, gRPC impl
│   ├── logging/                   # Structured logging, MDC, correlation ID propagation
│   └── observability/             # Micrometer metrics, Grafana dashboards, health config
├── .github/workflows/             # GitHub Actions CI/CD pipelines
├── README.md                      # Human-facing project documentation
├── AGENTS.md                      # This file — read before touching anything
├── compose.yml                    # Local dev infrastructure (Docker Compose)
├── Dockerfile                     # Multi-stage production image
├── gemini-tasks.md                # Gemini's action log
└── claude-tasks.md                # Claude's action log
```

---

## Ground Rules for All Agents

### 1. Read before you write
Always inspect the relevant `pom.xml`, existing classes, and `application.yml` before generating or modifying any file. Never assume a dependency is present — verify it first.

### 2. Respect module boundaries

The dependency direction is strictly one-way and must never be violated:

```
contract      ──▶  no deps on internal modules
domain        ──▶  no deps on internal modules
application   ──▶  domain, contract (ports only)
infrastructure ──▶ application, domain, contract, logging, observability
logging       ──▶  no deps on internal modules
observability ──▶  no deps on internal modules
```

- **`domain`** must never import Spring, JPA, Kafka, or any framework class — ever.
- **`application`** must never import infrastructure adapters directly — only port interfaces.
- **`contract`** owns all DTOs and event schemas. Never duplicate them in other modules.
- **`infrastructure`** is the only module allowed to have Spring Data, Kafka, RabbitMQ, gRPC, and DB driver dependencies.

### 3. Never touch another agent's ownership area without flagging it
See the **Ownership Map** below. If you need to modify a file owned by another agent, add a `## Cross-Agent Changes` entry to your tasks file explaining the why.

### 4. Log everything
Every action — files created, files modified, decisions made, things deliberately skipped — must be recorded in your `{MODEL-NAME}-TASKS.md` before ending your session. Format is defined in the **Tasks File Format** section below.

### 5. Do not delete files unilaterally
Prefer deprecating or renaming. If deletion is necessary, document the reason in your tasks file and get human confirmation.

### 6. Follow existing conventions
- Java package root: `turbo.diesel.skeletoni`
- Naming: `{Entity}Command`, `{Entity}CommandHandler`, `{Entity}Query`, `{Entity}QueryHandler`, `{Entity}CreatedEvent`
- MapStruct mappers live in `infrastructure` — never in `domain` or `application`
- Lombok is available in all modules — prefer `@Value`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`
- All config values must be externalized to `application.yml` — no hardcoded strings in Java source
- Cron expressions go under `app.scheduler.*` in `application.yml`

### 7. Tests are mandatory
Any class created by an agent must have a corresponding test. Unit tests go in the same module. Integration tests (Testcontainers) go in `infrastructure/src/test`.

### 8. No secrets in code
Never commit credentials, tokens, or passwords. Use `application-local.yml` (git-ignored) or environment variables defined in `compose.yml`.

---

## Ownership Map

| Area | Owner | Status | Notes |
|---|---|---|---|
| Root `pom.xml` | Gemini | ✅ Done | Multi-module parent |
| `code/pom.xml` | Gemini | ✅ Done | Container module POM |
| All sub-module `pom.xml` files | Gemini | ✅ Done | contract, application, domain, infrastructure, logging, observability |
| Project directory scaffolding | Gemini | ✅ Done | All module directories and base layout |
| `SkeletoniApplication.java` | Gemini | ✅ Done | Migrated to `code/application/src/main/java/turbo/diesel/skeletoni/` |
| `application.yml` | Gemini | ✅ Done | Migrated to `code/application/src/main/resources/` |
| `SkeletoniApplicationTests.java` | Gemini | ✅ Done | Migrated to `code/application/src/test/` |
| `README.md` | Claude | ✅ Done | Full architecture docs, tech stack, module guide |
| `AGENTS.md` | Claude | ✅ Done | This file |
| `claude-tasks.md` | Claude | ✅ Done | Claude's action log |
| Domain model classes | Unassigned | ⬜ Pending | — |
| Application use cases (CQRS) | Unassigned | ⬜ Pending | — |
| Infrastructure adapters | Unassigned | ⬜ Pending | — |
| `compose.yml` | Unassigned | ⬜ Pending | — |
| `Dockerfile` | Unassigned | ⬜ Pending | — |
| `.github/workflows/ci.yml` | Unassigned | ⬜ Pending | — |
| `.github/workflows/release.yml` | Unassigned | ⬜ Pending | — |
| `contract/openapi.yml` | Unassigned | ⬜ Pending | — |
| `contract/asyncapi.yml` | Unassigned | ⬜ Pending | — |
| Grafana dashboard JSON | Unassigned | ⬜ Pending | — |

---

## Tasks File Format

Every agent must maintain a `{MODEL-NAME}-TASKS.md` file at the **project root**. Use this exact structure:

```markdown
# {Model Name} — Action Plan & Tasks

## Action Plan
Brief description of the goal and overall strategy for this agent's scope.

1. Step one
2. Step two

## Tasks Done

- [x] **Task title**: What was done, which files were created or modified.

## Tasks Pending

- [ ] **Task title**: What is deferred and why.

## Decisions & Notes

- **Decision**: Why X was chosen over Y.
- **Note**: Anything the next agent or human should know.

## Files Created or Modified

| File | Action | Reason |
|---|---|---|
| `path/to/file.java` | Created | Short reason |
| `path/to/other.xml` | Modified | Short reason |
```

---

## Coordination Protocol

When multiple agents are active simultaneously (e.g. Claude in chat + Gemini in the IDE):

1. **Read `AGENTS.md` and both tasks files first** before starting any work.
2. **Check the ownership map** — if your target area is owned by another agent, flag it.
3. **Announce intent** — add a `## Tasks Pending` entry to your tasks file before starting long-running work so other agents know what's in progress.
4. **Commit atomically** — keep each agent's changes in focused, reviewable commits. Prefix commit messages: `[claude]`, `[gemini]`, `[human]`.
5. **Resolve conflicts here** — if ownership is disputed, a human updates the ownership map in this file.

---

## Approved Technology Stack

Agents must only use the following. Do not introduce alternatives without human approval and a parent `pom.xml` update.

| Category | Approved | Not Approved |
|---|---|---|
| Java | 21 | < 17 |
| Spring Boot | 4.0 | 2.x, 3.x |
| Build | Maven | Gradle |
| Messaging | Kafka, RabbitMQ | SQS, Pulsar, ActiveMQ |
| Databases | PostgreSQL, MongoDB, Couchbase | MySQL, Redis, H2 (except tests) |
| Migrations | Flyway | Liquibase |
| Resiliency | Resilience4j | Hystrix, Sentinel |
| Mapping | MapStruct | ModelMapper, manual mapping |
| Boilerplate | Lombok | — |
| Logging | SLF4J + Logback | Log4j2, java.util.logging |
| Testing | JUnit 5, Mockito, Testcontainers | JUnit 4, PowerMock |
| REST docs | OpenAPI 3 (SpringDoc) | Springfox |
| Async docs | AsyncAPI 3 | — |

---

## Module Decision Tree

When unsure where a new class belongs, follow this:

```
Is it a business rule, aggregate, or domain event?
  └─▶ domain

Does it orchestrate steps between domain and infrastructure?
  └─▶ application  (use a port interface, never import infra directly)

Is it a DTO, proto definition, or event schema shared with other services?
  └─▶ contract

Does it talk to a database, broker, HTTP client, or external system?
  └─▶ infrastructure

Does it deal with log formatting, MDC enrichment, or correlation IDs?
  └─▶ logging

Does it configure metrics, distributed tracing, or health checks?
  └─▶ observability
```

---

## Key Naming Conventions (Quick Reference)

```
Commands:        CreateExampleCommand, UpdateExampleCommand
Command Handler: CreateExampleCommandHandler
Queries:         GetExampleByIdQuery, ListExamplesQuery
Query Handler:   GetExampleByIdQueryHandler
Domain Events:   ExampleCreatedEvent, ExampleUpdatedEvent
Aggregates:      Example (no suffix)
Value Objects:   ExampleId, ExampleName
Ports (in):      ExampleUseCase
Ports (out):     ExampleRepository, ExampleEventPublisher
JPA Entities:    ExampleJpaEntity
JPA Repos:       ExampleJpaRepository  (Spring Data, infra only)
Mongo Docs:      ExampleDocument
Adapters:        ExamplePostgresAdapter, ExampleMongoAdapter, ExampleKafkaProducer
Mappers:         ExampleMapper  (MapStruct, infra only)
```