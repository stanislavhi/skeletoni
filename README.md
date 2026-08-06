# 🦴 skeletoni

> A modern, opinionated, multi-module Spring Boot 4 microservice skeleton.  
> Clone it. Rename it. Ship it.

---

## Overview

**skeletoni** is a production-ready starting point for Java microservices that need to be built right from day one. It combines event-driven architecture, CQRS, multi-protocol communication (REST + gRPC + Messaging), full observability, contract-first API design, and a clean hexagonal multi-module structure — so you spend zero time wiring and all your time building.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                               skeletoni                                  │
│                                                                          │
│  ┌─────────────┐   ┌─────────────┐   ┌──────────┐   ┌────────────────┐  │
│  │  contract   │   │ application │   │  domain  │   │ infrastructure │  │
│  │             │──▶│             │──▶│          │◀──│                │  │
│  │ OpenAPI 3   │   │ Use cases   │   │ Entities │   │ PostgreSQL     │  │
│  │ AsyncAPI 3  │   │ CQRS cmds   │   │ Aggreg.  │   │ MongoDB        │  │
│  │ gRPC proto  │   │ CQRS queries│   │ Events   │   │ Couchbase      │  │
│  │ REST DTOs   │   │ Schedulers  │   │ Services │   │ Kafka          │  │
│  │ Event DTOs  │   │ Ports       │   │          │   │ RabbitMQ       │  │
│  └─────────────┘   └─────────────┘   └──────────┘   │ gRPC stubs     │  │
│                                                      │ Resilience4j   │  │
│  ┌──────────────────┐   ┌──────────────────────────┐ └────────────────┘  │
│  │     logging      │   │       observability       │                    │
│  │                  │   │                           │                    │
│  │ SLF4J + Logback  │   │ Micrometer + Prometheus   │                    │
│  │ MDC Enrichment   │   │ Grafana Dashboards        │                    │
│  │ Correlation IDs  │   │ Actuator Health/Metrics   │                    │
│  │ Structured JSON  │   │ Distributed Tracing       │                    │
│  └──────────────────┘   └──────────────────────────┘                    │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 21 (Virtual Threads / Project Loom) |
| Framework | Spring Boot 4.0 |
| Build | Maven (multi-module) |
| REST API Docs | SpringDoc OpenAPI 3 + Swagger UI |
| Async API Docs | AsyncAPI 3 (Kafka + RabbitMQ event contracts) |
| RPC | gRPC (Protocol Buffers) |
| Messaging | Apache Kafka + RabbitMQ |
| Relational DB | PostgreSQL + Flyway |
| Document DB | MongoDB |
| Key-Value / Doc | Couchbase |
| Resiliency | Resilience4j (Circuit Breaker, Retry, Rate Limiter, Bulkhead) |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Observability | Micrometer + Prometheus + Grafana |
| Logging | SLF4J + Logback (structured JSON) |
| Scheduling | Spring `@Scheduled` + Virtual Thread executor |
| Code Quality | SonarCloud |
| CI/CD | GitHub Actions |
| Containerization | Docker + Docker Compose |
| Architecture | CQRS + Event-Driven (Hexagonal) |

---

## Module Structure

```
skeletoni/
├── pom.xml                                    # Root aggregation POM
├── LICENSE
├── CONTRIBUTING.md
├── AGENTS.md
├── README.md
├── compose.yml
├── Dockerfile
└── code/                                      # Source module container
    ├── pom.xml                                # Main parent POM — BOM + plugin management
    │
    ├── contract/                              # Public surface area — all API contracts
    │   ├── pom.xml
    │   └── src/main/resources/
    │       ├── openapi.yml                   # OpenAPI 3 REST spec
    │       ├── asyncapi.yml                  # AsyncAPI 3 event/messaging spec
    │       └── proto/                        # Protobuf definitions for gRPC
    │           └── example.proto
    │
    ├── application/                               # Orchestration layer — no framework deps
    │   ├── pom.xml
    │   └── src/main/java/.../application/
    │       ├── command/                           # CQRS write side
    │       ├── query/                             # CQRS read side
    │       ├── web/                               # REST controllers
    │       ├── service/                          # Use case implementations
    │       └── port/                             # Inbound & outbound port interfaces
    │
    ├── domain/                                    # Pure business logic — zero deps
    │   ├── pom.xml
    │   └── src/main/java/.../domain/
    │       ├── model/                             # Aggregates, entities, value objects
    │       ├── event/                             # Domain events
    │       └── service/                          # Domain services
    │
    ├── infrastructure/                            # All framework & I/O adapters
    │   ├── pom.xml
    │   └── src/main/java/.../infrastructure/
    │       ├── adapter/                          # Persistence, messaging, gRPC adapters
    │       ├── entity/                           # JPA/Mongo/Couchbase entities
    │       ├── repository/                       # Spring Data repositories
    │       ├── mapper/                           # MapStruct mappers
    │       └── src/main/resources/
    │           └── db/migration/                 # Flyway scripts (V1__init.sql ...)
    │
    ├── boot/                                      # Composition Root / Startup
    │   ├── pom.xml
    │   └── src/main/resources/
    │       ├── application.yml
    │       └── logback-spring.xml
    │
    ├── logging/                                   # Cross-cutting structured logging
    │   └── pom.xml
    │
    ├── observability/                             # Metrics, tracing, health
    │   └── pom.xml
    │
    └── resilience/                                # Fault tolerance patterns
        └── pom.xml
```

---

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

---

## Getting Started

### 1. Clone and rename

```bash
git clone https://github.com/your-org/skeletoni.git my-service
cd my-service
```

Do a project-wide find & replace of `skeletoni` → your service name in:

- All `pom.xml` files (`artifactId`, `name`, `groupId`)
- `application.yml` (`spring.application.name`)
- All Java package names under `src/`
- Proto package declarations in `.proto` files
- `asyncapi.yml` and `openapi.yml` info blocks

### 2. Start infrastructure

```bash
docker compose up -d
```

This brings up: PostgreSQL, MongoDB, Couchbase, Kafka + Kafka UI, RabbitMQ Management, Prometheus, and Grafana.

### 3. Run the service

```bash
mvn spring-boot:run -f code/boot/pom.xml
```

The service starts on **http://localhost:8080**.

---

## API & Contract Docs

| Interface | URL | Description |
|---|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html | Interactive REST explorer |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | OpenAPI 3 machine-readable spec |
| AsyncAPI spec | `contract/src/main/resources/asyncapi/asyncapi.yml` | Kafka + RabbitMQ event contracts |
| AsyncAPI Studio | https://studio.asyncapi.com | Paste the spec to visualize |
| gRPC | `localhost:9090` | Protobuf-based RPC |
| Kafka UI | http://localhost:8081 | Browse topics, messages, consumer lag |
| RabbitMQ Mgmt | http://localhost:15672 | Exchanges, queues, bindings (guest/guest) |

---

## CQRS & Event-Driven Design

Commands and queries are strictly separated at the application layer. The domain emits events that drive both internal read-model projections and cross-service integration.

```
HTTP/gRPC ──▶ Controller ──▶ CommandBus ──▶ CommandHandler ──▶ Aggregate
                                                                    │
                                                        DomainEvent emitted
                                                                    │
                                           ┌────────────────────────┴────────────────────────┐
                                      Kafka topic                                  RabbitMQ exchange
                                     (durable, replay)                           (routing, fanout)
                                           │
                                  ┌────────┴────────┐
                             Projector           Downstream
                          (MongoDB read        service consumer
                           model update)
```

- **Write model** — PostgreSQL, strongly consistent, transactional
- **Read model** — MongoDB or Couchbase, denormalized, query-optimized
- **Events** — Kafka for durable cross-service events; RabbitMQ for internal routing and fanout

---

## AsyncAPI — Messaging Contracts

All Kafka topics and RabbitMQ exchanges/queues consumed or published by this service are documented in `contract/src/main/resources/asyncapi/asyncapi.yml` following the **AsyncAPI 3** specification.

This file is the single source of truth for:

- Which topics/queues this service **publishes** to (outbound events)
- Which topics/queues this service **subscribes** to (inbound events)
- The schema (JSON Schema / Avro reference) of every message payload
- Bindings for Kafka partition keys, RabbitMQ routing keys, headers

Teams consuming this service's events should reference the AsyncAPI spec the same way frontend teams reference an OpenAPI spec.

---

## Messaging

### Kafka

Topics and consumer group IDs are declared in `application.yml`. Producers use `KafkaTemplate`. Consumers use `@KafkaListener` with dead-letter topic (DLT) support via `@RetryableTopic`.

### RabbitMQ

Exchanges, queues, and bindings are declared as `@Bean` in `infrastructure/config/RabbitMQConfig.java`. Consumers use `@RabbitListener`.

---

## Databases

| Store | Role | Module |
|---|---|---|
| PostgreSQL + Flyway | Write model — transactional, consistent | `infrastructure/postgres` |
| MongoDB | Read model — projections, document queries | `infrastructure/mongodb` |
| Couchbase | Key-value lookups, distributed cache-like access | `infrastructure/couchbase` |

Flyway migrations run automatically on startup. Naming convention: `V{n}__{description}.sql` — e.g. `V1__create_example_table.sql`.

---

## Resiliency

Resilience4j patterns are configured per outbound integration in `infrastructure/resilience/`:

| Pattern | Applied to |
|---|---|
| Circuit Breaker | All outbound HTTP calls, DB adapters |
| Retry + backoff | Kafka producers, HTTP clients |
| Rate Limiter | Public API endpoints |
| Bulkhead | Thread pool isolation per external dependency |

---

## Observability

### Metrics & Dashboards

```
http://localhost:8080/actuator/prometheus   ← Prometheus scrape endpoint
http://localhost:9090                       ← Prometheus UI
http://localhost:3000                       ← Grafana (admin / admin)
```

A pre-built Grafana dashboard (`observability/dashboards/skeletoni-dashboard.json`) covers JVM memory, GC, HTTP latency, Kafka consumer lag, RabbitMQ queue depth, and custom business metrics.

### Health Endpoints

```
GET /actuator/health          ← Liveness + readiness + all component statuses
GET /actuator/info            ← Build info + git commit
GET /actuator/metrics         ← All registered Micrometer meters
GET /actuator/prometheus      ← Prometheus-format scrape target
```

### Structured Logging

Every log line is emitted as JSON and enriched with:

| Field | Source |
|---|---|
| `correlationId` | `X-Correlation-ID` header (generated if absent) |
| `traceId` / `spanId` | Micrometer Tracing (W3C Trace Context) |
| `service` | `spring.application.name` |
| `env` | `spring.profiles.active` |
| `version` | `pom.xml` project version |

The `logging` module provides a servlet filter and Kafka interceptor that inject these fields into MDC automatically — no manual MDC calls needed in application code.

---

## Scheduling

Jobs are declared in `application/scheduler/` using `@Scheduled`. All cron expressions are externalized to `application.yml` under `app.scheduler.*`. The scheduler executor is configured to use **Virtual Threads** (Project Loom), so blocking jobs don't consume platform threads.

---

## Testing Strategy

```bash
mvn test   -f code/pom.xml   # Unit tests only
mvn verify -f code/pom.xml   # Full build
```

| Layer | Scope | Tools |
|---|---|---|
| `domain` | Pure unit — aggregates, domain services | JUnit 5 + Mockito |
| `application` | Use case unit tests with port mocks | JUnit 5 + Mockito |
| `contract` | Controller slice tests | `@WebMvcTest` + MockMvc |
| `infrastructure` | Full integration — real DB, real broker | Testcontainers |

Testcontainers manages real PostgreSQL, MongoDB, Kafka, and RabbitMQ containers for integration tests. No in-memory fakes, no manual setup.

---

## Code Quality

SonarCloud analysis runs on every push to `main` via GitHub Actions. To analyze locally:

```bash
mvn sonar:sonar -f code/pom.xml \
  -Dsonar.projectKey=your-org_skeletoni \
  -Dsonar.organization=your-org \
  -Dsonar.token=$SONAR_TOKEN
```

Quality gate covers: coverage thresholds, duplication, code smells, security hotspots, and bug detection.

---

## CI/CD — GitHub Actions

Two workflows in `.github/workflows/`:

### `ci.yml` — triggered on every push and PR to `main`

```
Checkout → Java 21 setup → Maven build → Unit tests
    → Integration tests (Testcontainers) → SonarCloud → Docker build
```

### `release.yml` — triggered on `v*` tag push

```
Checkout → Java 21 setup → Maven build → Full test suite
    → Docker build + push to registry → GitHub Release notes
```

Secrets required: `SONAR_TOKEN`, `DOCKER_USERNAME`, `DOCKER_PASSWORD` (or equivalent registry credentials).

---

## Configuration Reference

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP port |
| `GRPC_PORT` | `9090` | gRPC port |
| `SPRING_DATASOURCE_URL` | see `compose.yml` | PostgreSQL JDBC URL |
| `SPRING_DATA_MONGODB_URI` | see `compose.yml` | MongoDB connection URI |
| `SPRING_COUCHBASE_CONNECTION_STRING` | see `compose.yml` | Couchbase cluster address |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker list |
| `SPRING_RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `MANAGEMENT_PROMETHEUS_ENABLED` | `true` | Expose Prometheus scrape endpoint |
| `SONAR_TOKEN` | *(CI secret)* | SonarCloud authentication token |

---

## Minimum Viable Customization

To turn this skeleton into a real service:

1. Rename `skeletoni` everywhere
2. Define your aggregate in `domain/model/`
3. Write your first Flyway migration in `db/migration/V1__init.sql`
4. Add a command + handler in `application/command/`
5. Expose it via a REST controller in `contract/rest/`
6. Define your Kafka topic and AsyncAPI contract in `asyncapi.yml`
7. Write one Testcontainers integration test

Everything else — resiliency, metrics, distributed tracing, structured logging, CI pipeline — works out of the box.

---

## License

[MIT](LICENSE)