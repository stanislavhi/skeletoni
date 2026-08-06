# 🦴 skeletoni

> A multi-module Spring Boot 4 microservice skeleton.
> Clone it. Rename it. Ship it.

---

## Overview

**skeletoni** is a starting point for Java microservices: a strict hexagonal multi-module Maven
layout, contract-first API design, and the observability and build plumbing already wired.

It ships **one fully working vertical slice** — the `Example` aggregate, exposed over REST and
persisted to PostgreSQL through a port/adapter pair — plus scaffolding for the protocols and stores
a service typically grows into.

> **Read this first.** Sections below are marked either **Implemented** (working and exercised) or
> **Planned** (dependencies and configuration present, no code behind them yet). The skeleton's
> value is the structure and the one honest end-to-end slice, not a pre-built feature set. Do not
> assume a capability exists because a dependency is on the classpath.

---

## What works today

| Capability | State |
|---|---|
| REST slice (`POST`/`GET /api/v1/examples`), contract-first | **Implemented** |
| PostgreSQL persistence, Flyway migration, port/adapter + MapStruct | **Implemented** |
| Hexagonal module boundaries enforced by Maven | **Implemented** |
| Correlation ID propagation (`X-Correlation-Id` → MDC → logs) | **Implemented** |
| Actuator health + Micrometer/Prometheus metrics + Grafana dashboard | **Implemented** |
| Local stack via Docker Compose; CI build, contract lint, image build | **Implemented** |
| Resilience4j circuit breaker + retry | Configured, **no call site yet** |
| gRPC, Kafka, RabbitMQ, MongoDB, Couchbase, Avro | Dependencies + config only, **no adapters** |
| CQRS command/query handlers, domain events, scheduling, tracing | **Planned** |

Open work is tracked as `SKL-` tickets in [`lode/plans/backlog.md`](lode/plans/backlog.md).

---

## Architecture

Dependency direction is one-way and enforced by Maven — a forbidden import does not compile,
because the dependency is absent from the POM.

```
                    ┌──────────────┐
                    │     boot     │  composition root, runnable jar
                    └──────┬───────┘
           ┌───────────────┼────────────────┬──────────────┐
           ▼               ▼                ▼              ▼
  ┌────────────────┐  ┌─────────┐  ┌───────────────┐  ┌────────────┐
  │ infrastructure │  │ logging │  │ observability │  │ resilience │
  └───────┬────────┘  └─────────┘  └───────────────┘  └────────────┘
          ▼
  ┌───────────────┐        ┌──────────┐
  │  application  │───────▶│ contract │   DTOs, OpenAPI/AsyncAPI/proto
  └───────┬───────┘        └──────────┘
          ▼
  ┌───────────────┐
  │    domain     │   pure Java — no Spring, no JPA, zero dependencies
  └───────────────┘
```

| Module | Role | Framework allowed |
|---|---|---|
| `contract` | API definitions, DTOs, REST controller + delegate interface | Spring Web, SpringDoc, gRPC stubs |
| `domain` | Aggregates, value objects, domain events | **none** |
| `application` | Use cases, commands, in/out ports, delegate impls | `spring-context` only |
| `infrastructure` | All I/O — JPA, messaging, external clients, MapStruct | everything |
| `logging` | Correlation ID filter, MDC enrichment | Spring Web |
| `observability` | Micrometer config, health indicators | Actuator, Micrometer |
| `resilience` | Resilience4j wiring | Spring Cloud CircuitBreaker |
| `boot` | Composition root, `application.yml`, runnable jar | everything |

Detailed topology: [`lode/architecture/module-topology.md`](lode/architecture/module-topology.md).

---

## Tech Stack

### Wired and exercised

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0 |
| Build | Maven (multi-module; reactor root is `code/pom.xml`) |
| REST API Docs | SpringDoc OpenAPI 3 + Swagger UI |
| Relational DB | PostgreSQL + Flyway 9.x |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Observability | Micrometer + Prometheus + Grafana |
| Logging | SLF4J + Logback |
| Code Quality | SonarCloud |
| CI/CD | GitHub Actions |
| Containerization | Docker + Docker Compose |
| Architecture | Hexagonal (ports & adapters) |

### On the classpath, not yet used

| Concern | Technology | Status |
|---|---|---|
| RPC | gRPC (Protocol Buffers) | `.proto` + codegen + server starter; **no service impl** |
| Messaging | Kafka, RabbitMQ, Spring Cloud Stream | binders configured; **no producers or consumers** |
| Async API Docs | AsyncAPI 3 | spec written; nothing publishes to it |
| Document DB | MongoDB | starter only |
| Key-Value / Doc | Couchbase | starter only |
| Serialization | Avro + Confluent Schema Registry | plugin wired; **no schemas** |
| Resiliency | Resilience4j (circuit breaker, retry) | configured; **not applied to any call** |

Rate limiter, bulkhead, distributed tracing, virtual-thread executors and CQRS buses are **not**
present in any form.

---

## Module Structure

```
skeletoni/
├── pom.xml                     # thin aggregator — declares only <module>code</module>
├── compose.yml                 # local infrastructure
├── Dockerfile                  # multi-stage build
├── .spectral.yaml              # ruleset for the CI contract lint
├── infra/
│   ├── prometheus/prometheus.yml
│   └── grafana/                # provisioned datasource + dashboards (versioned as code)
├── lode/                       # project knowledge base + SKL backlog
└── code/                       # ← the real Maven reactor root
    ├── pom.xml                 # parent POM — BOM, plugin + annotation-processor management
    │
    ├── contract/
    │   └── src/main/
    │       ├── java/.../contract/
    │       │   ├── dto/        # CreateExampleRequest, ExampleResponse
    │       │   └── web/        # ExampleController, ExampleControllerDelegate
    │       └── resources/
    │           ├── openapi.yml
    │           ├── asyncapi.yml
    │           ├── proto/example.proto
    │           └── avro/       # empty (.gitkeep is load-bearing — see note below)
    │
    ├── application/
    │   └── src/main/java/.../application/
    │       ├── command/        # CreateExampleCommand
    │       ├── port/in/        # ExampleUseCase
    │       ├── port/out/       # ExampleRepository
    │       ├── service/        # ExampleService, ExampleControllerDelegateImpl
    │       ├── dto/            # empty — reserved
    │       └── query/          # empty — reserved for the CQRS read side
    │
    ├── domain/
    │   └── src/main/java/.../domain/
    │       ├── model/          # Example, ExampleId
    │       ├── event/          # ExampleCreatedEvent (declared, never published)
    │       ├── service/        # empty — reserved
    │       └── exception/      # empty — reserved
    │
    ├── infrastructure/
    │   ├── src/main/java/.../infrastructure/
    │   │   ├── adapter/persistence/   # ExamplePostgresAdapter
    │   │   ├── entity/                # ExampleJpaEntity
    │   │   ├── repository/            # ExampleJpaRepository
    │   │   └── mapper/                # ExampleMapper (MapStruct)
    │   ├── src/main/resources/db/migration/   # V1__init_examples.sql
    │   └── src/test/java/...           # ExamplePostgresAdapterIT (Testcontainers)
    │
    ├── logging/          # CorrelationIdFilter
    ├── observability/    # MetricsConfig, ApplicationHealthIndicator
    ├── resilience/       # ResilienceConfig
    └── boot/             # SkeletoniApplication, application.yml, logback-spring.xml
```

Directories marked *empty — reserved* exist to signal intended placement. They contain no code.

> `code/contract/src/main/resources/avro/.gitkeep` must stay. `avro-maven-plugin` fails the build
> when that directory is missing, and git does not track empty directories — without it the build
> passes locally and fails in CI.

---

## Prerequisites

- Java 21+
- Maven 3.9+ (no wrapper is committed — install Maven yourself)
- Docker & Docker Compose

---

## Getting Started

### 1. Clone and rename

```bash
git clone https://github.com/your-org/skeletoni.git my-service
cd my-service
```

Find & replace `skeletoni` → your service name in:

- All `pom.xml` files (`artifactId`, `name`, `groupId`)
- `code/boot/src/main/resources/application.yml` (`spring.application.name`)
- Java package names under `code/*/src/`
- `package` and `java_package` in `code/contract/src/main/resources/proto/example.proto`
- The `info` blocks of `openapi.yml` and `asyncapi.yml`

### 2. Start infrastructure

```bash
docker compose up -d
```

Brings up PostgreSQL, MongoDB, Couchbase, Kafka + Kafka UI + Schema Registry, RabbitMQ, Prometheus
and Grafana. Only PostgreSQL is required by the code that exists today.

### 3. Run the service

```bash
mvn spring-boot:run -f code/boot/pom.xml
```

Starts on **http://localhost:8080** with the `local` profile, which reads
`code/boot/src/main/resources/application-local.yml`. That file is git-ignored; copy
`application-local.yml.example` if it is missing.

Every Maven command targets `code/pom.xml`, never the repo root:

```bash
mvn clean install -f code/pom.xml -DskipTests   # full rebuild
mvn -B clean verify -f code/pom.xml             # what CI runs
mvn generate-sources -pl code/contract          # regenerate protobuf/gRPC stubs
```

---

## API & Contract Docs

| Interface | URL / path |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| OpenAPI spec | `code/contract/src/main/resources/openapi.yml` |
| AsyncAPI spec | `code/contract/src/main/resources/asyncapi.yml` |
| gRPC port | `localhost:9091` *(configured; no service implemented)* |
| Kafka UI | http://localhost:8081 |
| Schema Registry | http://localhost:8085 |
| RabbitMQ Mgmt | http://localhost:15672 (`user` / `password`) |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (`admin` / `admin`) |

Both YAML contracts are linted in CI by Spectral using the root `.spectral.yaml`.

---

## The REST slice — how a request flows

The one path wired end to end. Reproduce this shape for new features.

```
POST /api/v1/examples
  → CorrelationIdFilter            (logging)         MDC + response header
  → ExampleController              (contract)        DTO in
  → ExampleControllerDelegateImpl  (application)     DTO → command → domain
  → ExampleService                 (application)     Example.create(...)
  → ExampleRepository              (application)     out-port
  → ExamplePostgresAdapter         (infrastructure)  MapStruct → JPA → PostgreSQL
```

`contract` cannot see `domain`, so the controller talks to `ExampleControllerDelegate`, implemented
in `application`. Compile-time dependency runs `application → contract`; the runtime call runs the
other way. Details: [`lode/contract/rest-delegate-pattern.md`](lode/contract/rest-delegate-pattern.md).

---

## Databases

| Store | Role | State |
|---|---|---|
| PostgreSQL + Flyway | Write model — transactional | **Implemented** — `infrastructure/adapter/persistence` |
| MongoDB | Intended read model | starter on classpath, no adapter |
| Couchbase | Intended key-value access | starter on classpath, no adapter |

Flyway migrations live in `code/infrastructure/src/main/resources/db/migration` and run before
Hibernate validates the schema (`ddl-auto: validate`). Naming: `V{n}__{description}.sql`.

Flyway is pinned to **9.22.3** deliberately — 10.x splits database support into separate artifacts
that resolve unreliably through the Confluent mirror this build uses for Avro.

---

## Observability

### Metrics & dashboards

```
http://localhost:8080/actuator/prometheus   ← scrape endpoint
http://localhost:9090                       ← Prometheus UI
http://localhost:3000                       ← Grafana (admin / admin)
```

`infra/grafana/dashboards/skeletoni-jvm.json` is provisioned automatically and covers **JVM and
HTTP built-ins**. There are no custom business metrics yet. Dashboards are versioned as code —
edit the JSON in the repo, not the Grafana UI, or changes are lost on `docker compose down -v`.

### Health endpoints

```
GET /actuator/health       GET /actuator/info
GET /actuator/metrics      GET /actuator/prometheus
```

`show-details` is `when-authorized` by default and `always` only on the `local` profile.
`ApplicationHealthIndicator` currently returns a constant `UP` — it is an extension point, not a
real readiness check.

### Correlation IDs

`CorrelationIdFilter` reuses an inbound `X-Correlation-Id` or generates one, puts it in SLF4J MDC
for the request, echoes it on the response, and always clears it in a `finally`. The console
pattern includes `[%X{correlationId:-}]`.

Two limits worth knowing:

- **Servlet requests only.** Scheduled tasks, `@Async` work and future Kafka listeners get no
  correlation ID.
- **Not propagated outbound**, and there is no distributed tracing dependency — the ID stops at
  this service's boundary.

`logback-spring.xml` defines a Logstash JSON appender that is **not attached** to the root logger,
so console output is plain text today.

---

## Testing

```bash
mvn test   -f code/pom.xml   # unit tests
mvn verify -f code/pom.xml   # full build
```

| Layer | Present |
|---|---|
| `boot` | `SkeletoniApplicationTests` — context load on H2, `@ActiveProfiles("test")` |
| `infrastructure` | `ExamplePostgresAdapterIT` — Testcontainers PostgreSQL 17 |

That is the entire suite. `domain`, `application`, `contract` and `logging` have **no tests yet**.

> **`*IT` classes do not currently execute.** `maven-failsafe-plugin` is unconfigured and Surefire's
> default includes do not match the `IT` suffix, so `ExamplePostgresAdapterIT` compiles and is
> silently skipped — `mvn verify` reports `Tests run: 1` while passing. Tracked as `SKL-32`.

The `test` profile uses in-memory H2 and excludes Mongo, Couchbase, Kafka and RabbitMQ
auto-configuration. It also supplies dummy values for mandatory placeholders — excluding an
auto-configuration does not stop property resolution, so a new mandatory `${VAR}` in
`application.yml` needs a matching dummy in `application-test.yml` or every `@SpringBootTest` fails.

---

## Code Quality

SonarCloud analysis runs in CI when `SONAR_TOKEN` is set; the build degrades gracefully without it
(useful on forks). To analyze locally:

```bash
mvn sonar:sonar -f code/pom.xml \
  -Dsonar.projectKey=your-org_skeletoni \
  -Dsonar.organization=your-org \
  -Dsonar.token=$SONAR_TOKEN
```

Exclusions live in **two** places that must stay in sync: `sonar.exclusions` in `code/pom.xml` and
`sonar-project.properties`. Note rule `java:S6204` — use `Stream.toList()`, never
`collect(Collectors.toList())`.

---

## CI/CD — GitHub Actions

### `ci.yml` — every push, every PR, plus manual dispatch

Three jobs:

| Job | Does |
|---|---|
| `build` | JDK 21, `mvn -B clean verify -f code/pom.xml`, optional SonarCloud, uploads surefire reports |
| `build-docker` | needs `build`; builds the image with `push: false` — validation only |
| `lint-contracts` | Spectral lint of `openapi.yml` and `asyncapi.yml` |

Service containers (PostgreSQL, MongoDB, Kafka, Schema Registry) start without health gating, so
prefer Testcontainers for anything that needs a real dependency.

Do **not** add `SPRING_DATASOURCE_*` to the workflow environment. Environment variables outrank
profile YAML, so they replace the H2 url in `application-test.yml` while leaving
`driver-class-name: org.h2.Driver` untouched — producing a Postgres url driven by the H2 driver.

### `release.yml` — on `v*` tag push

Builds with `-DskipTests` and attaches `code/boot/target/*.jar` to a GitHub Release.

Two caveats: it **skips tests**, so a tag can ship a jar CI never validated; and **no workflow
publishes a Docker image**. No release has been cut yet — the project is still `0.0.1-SNAPSHOT`
with no tags.

Secrets: `SONAR_TOKEN` (optional), `GITHUB_TOKEN` (automatic).

---

## Configuration Reference

Sensitive values use `${VAR}` with **no default**, so a misconfigured environment fails fast at
startup rather than silently using a dev credential. Non-sensitive endpoints get defaults.

| Variable | Default | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | Active profile |
| `SERVER_PORT` | `8080` | HTTP port |
| `SPRING_DATASOURCE_URL` | *(none)* | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | *(none)* | PostgreSQL credentials |
| `SPRING_DATA_MONGODB_URI` | *(none)* | MongoDB URI |
| `SPRING_DATA_COUCHBASE_CONNECTION_STRING` | *(none)* | Couchbase host |
| `SPRING_DATA_COUCHBASE_USERNAME` / `_PASSWORD` / `_BUCKET_NAME` | *(none)* | Couchbase access |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `SPRING_KAFKA_SCHEMA_REGISTRY_URL` | `http://localhost:8085` | Schema Registry |
| `SPRING_KAFKA_CONSUMER_GROUP_ID` | `skeletoni-group` | Consumer group |
| `SPRING_RABBITMQ_HOST` / `_PORT` | `localhost` / `5672` | RabbitMQ broker |
| `SPRING_RABBITMQ_USERNAME` / `_PASSWORD` | *(none)* | RabbitMQ credentials |

The gRPC port is `grpc.server.port` (default `9091`) in `application.yml`, not an environment
variable. Full reference: [`lode/local-dev/configuration.md`](lode/local-dev/configuration.md).

---

## Planned — design intent, not yet built

These describe where the skeleton is headed. **None of it is implemented.** Kept here because the
structure anticipates it, and because the intended shape is useful when you build it yourself.

### CQRS & event-driven

Commands and queries separated at the application layer; the domain emits events driving read-model
projections and cross-service integration.

```
Controller ──▶ CommandHandler ──▶ Aggregate ──▶ DomainEvent
                                                    │
                                    ┌───────────────┴───────────────┐
                               Kafka topic                  RabbitMQ exchange
                            (durable, replay)              (routing, fanout)
```

Today `CreateExampleCommand` exists with no handler; both reads and writes go through
`ExampleService`. `ExampleCreatedEvent` is declared and never published — the natural next step is
an `ExampleEventPublisher` out-port with a Kafka adapter emitting to `example-topic`, matching the
payload already described in `asyncapi.yml`.

### Messaging

`spring.cloud.stream` binds `exampleConsumer-in-0` to `example-topic`, but **no `exampleConsumer`
bean exists** — the binding will not resolve at runtime. Kafka is currently configured for JSON
serialization despite the Avro tooling being present; that inconsistency needs a decision.

### Resiliency

`resilience4j` instances named `exampleService` are fully configured (10s window, 50% failure
threshold, 5s open, 3 half-open trials; retry 3× with exponential backoff) and
`ResilienceConfig` logs every state transition. **No method is annotated**, so the registries stay
empty. Apply them to a genuinely remote call — a Kafka publish or gRPC client — not to the
PostgreSQL adapter, where HikariCP timeouts are the right tool.

### Scheduling, tracing, read models

No `@Scheduled` beans, no `@EnableScheduling`, no tracing dependency, no MongoDB or Couchbase
adapters, no virtual-thread executor configuration.

---

## Minimum Viable Customization

1. Rename `skeletoni` everywhere (see Getting Started)
2. Define your aggregate in `code/domain/src/main/java/.../domain/model/`
3. Write your first Flyway migration in `code/infrastructure/src/main/resources/db/migration/`
4. Add the use case: in-port in `application/port/in/`, implementation in `application/service/`
5. Add the out-port in `application/port/out/` and its adapter in `infrastructure/adapter/`
6. Declare the endpoint in `openapi.yml`, then the DTOs and controller in `contract/`, and the
   delegate implementation in `application/service/`
7. Write a unit test for the use case and a Testcontainers test for the adapter — and configure
   `maven-failsafe-plugin` first, or the `*IT` test will not run

Working out of the box: module boundaries, correlation IDs, metrics, health, the local stack, and
the CI pipeline. Everything under **Planned** is yours to build.

---

## Further Reading

| Document | Contents |
|---|---|
| [`lode/lode-map.md`](lode/lode-map.md) | Index of the project knowledge base |
| [`lode/plans/backlog.md`](lode/plans/backlog.md) | `SKL-` ticket board |
| [`lode/plans/known-gaps.md`](lode/plans/known-gaps.md) | Current divergences between docs and code |
| [`AGENTS.MD`](AGENTS.MD) | Rules for AI agents contributing to this repo |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Human contributor workflow |
| [`code/CHANGELOG.md`](code/CHANGELOG.md) | Keep a Changelog, SemVer |

---

## License

[MIT](LICENSE)
