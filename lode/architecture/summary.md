# Architecture — Summary

skeletoni is a **hexagonal (ports & adapters) multi-module Maven** service. Physical Maven module
boundaries — not package conventions — enforce the architecture: a forbidden import simply does
not compile because the dependency is absent from the POM.

## Modules

| Module | Role | Framework allowed |
|---|---|---|
| `contract` | OpenAPI/AsyncAPI/proto definitions, DTOs, REST controller + delegate interface | Spring Web, SpringDoc, gRPC stubs |
| `domain` | Aggregates, value objects, domain events. Pure Java | **None** |
| `application` | Use cases, commands, in/out ports, delegate implementations | `spring-context` only |
| `infrastructure` | All I/O: JPA, Mongo, Couchbase, Kafka, RabbitMQ, gRPC server, MapStruct mappers | Everything |
| `logging` | Correlation ID filter, MDC enrichment, Logstash encoder | Spring Web |
| `observability` | Micrometer config, custom health indicator | Actuator, Micrometer |
| `resilience` | Resilience4j wiring and event logging | Spring Cloud CircuitBreaker |
| `boot` | Composition root, `application.yml`, `logback-spring.xml`, runnable jar | Everything |

## Layer diagram

```mermaid
flowchart TB
  subgraph edge["Edge"]
    REST["REST /api/v1/examples"]
    GRPC["gRPC :9091 (not implemented)"]
    MSG["Kafka / RabbitMQ (not implemented)"]
  end

  subgraph contract["contract"]
    CTRL["ExampleController"]
    DEL["ExampleControllerDelegate (interface)"]
    DTO["CreateExampleRequest / ExampleResponse"]
  end

  subgraph application["application"]
    IMPL["ExampleControllerDelegateImpl"]
    UC["ExampleUseCase (in-port)"]
    SVC["ExampleService"]
    OUT["ExampleRepository (out-port)"]
  end

  subgraph domain["domain — no framework"]
    AGG["Example / ExampleId"]
    EVT["ExampleCreatedEvent"]
  end

  subgraph infrastructure["infrastructure"]
    ADP["ExamplePostgresAdapter"]
    MAP["ExampleMapper (MapStruct)"]
    JPA["ExampleJpaRepository"]
  end

  DB[("PostgreSQL")]

  REST --> CTRL --> DEL
  IMPL -.implements.-> DEL
  IMPL --> UC
  SVC -.implements.-> UC
  SVC --> OUT
  ADP -.implements.-> OUT
  SVC --> AGG
  ADP --> MAP --> JPA --> DB
  GRPC -.->|missing| infrastructure
  MSG -.->|missing| infrastructure
```

## Key architectural decisions

1. **Physical module boundaries over package discipline** — prevents the big-ball-of-mud drift that
   package-only hexagonal layouts suffer from.
2. **Delegate indirection at the REST edge** — the controller lives in `contract` (which cannot see
   `domain`), so mapping domain→DTO happens in `application`. See
   [../contract/rest-delegate-pattern.md](../contract/rest-delegate-pattern.md).
3. **MapStruct over reflection mappers** — compile-time generation, errors surface at build time.
4. **Env-var indirection for all secrets** — `${VAR}` with no default for sensitive values so a
   misconfigured environment fails fast at startup rather than silently using a dev credential.
5. **Multi-stage Dockerfile** — Maven build stage separated from a JRE-only runtime stage.

## Detail lodes

- [module-topology.md](module-topology.md) — exact dependency edges and how to keep them legal
- [request-flow.md](request-flow.md) — the `POST /api/v1/examples` path, call by call
