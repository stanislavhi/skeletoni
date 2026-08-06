# skeletoni — Living Summary

**skeletoni** is an opinionated, production-shaped Spring Boot 4 microservice *skeleton*
(`groupId: turbo.diesel`, `artifactId: skeletoni`, version `0.0.1-SNAPSHOT`, Java 21) whose
purpose is to be cloned and renamed rather than deployed. It demonstrates a strict hexagonal
multi-module Maven layout under `code/` — `contract`, `domain`, `application`, `infrastructure`,
`logging`, `observability`, `resilience`, `boot` — wired end-to-end by a single vertical slice:
the `Example` aggregate, exposed over REST (`/api/v1/examples`), persisted to PostgreSQL via a
port/adapter pair, and documented contract-first in OpenAPI 3.1, AsyncAPI 3.0 and protobuf.
Everything around that slice is scaffolding that is *configured but not yet exercised*: Kafka,
RabbitMQ, Spring Cloud Stream, gRPC, MongoDB, Couchbase and Avro all have dependencies, compose
services and YAML, but no adapters. Local infrastructure comes up with `docker compose up -d`
(Postgres, Mongo, Couchbase, Kafka + Schema Registry + kafka-ui, RabbitMQ, Prometheus, Grafana);
CI builds with Maven, gates on SonarCloud, lints the API contracts with Spectral, and builds the
multi-stage Docker image. The repository is also a multi-agent workspace: `AGENTS.MD` defines
module-boundary rules, an ownership map and per-agent `{MODEL}-TASKS.md` logs.

## Entry points

- Architecture and module rules → [architecture/summary.md](architecture/summary.md)
- Language of the domain → [terminology.md](terminology.md)
- Conventions you must not break → [practices.md](practices.md)
- What to work on next → [plans/backlog.md](plans/backlog.md) (the `SKL-` board)
- What is currently broken or missing → [plans/known-gaps.md](plans/known-gaps.md)
- Releases, changelog, commit conventions → [build/versioning-and-releases.md](build/versioning-and-releases.md)
- Full index → [lode-map.md](lode-map.md)

## Current state at a glance

> **`mvn verify` is green but runs one test.** `maven-failsafe-plugin` is unconfigured, so every
> `*IT` class compiles and is silently skipped. The Testcontainers integration test has never
> executed. Tracked as `SKL-32`, top of [plans/backlog.md](plans/backlog.md).

| Area | State |
|---|---|
| REST slice (`Example`) | Working end-to-end, contract-first |
| PostgreSQL persistence | Working, Flyway `V1__init_examples.sql`; IT compiles but never runs (`SKL-32`) |
| Correlation ID / MDC | Working (`CorrelationIdFilter`) |
| Metrics / health | Working (Actuator + Micrometer/Prometheus + Grafana dashboard) |
| Resilience4j | Configured + event logging; **no annotated call sites** |
| Kafka / RabbitMQ / Cloud Stream | Dependencies + YAML only; **no producer/consumer beans** |
| gRPC | `.proto` + codegen + `grpc-server-spring-boot-starter`; **no service impl** |
| MongoDB / Couchbase / Avro | Dependencies only; **no documents, no schemas** |
