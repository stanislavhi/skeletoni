# Epic SKL-DOC — Documentation Truth

Documentation that contradicts the code is worse than none — it is actively misleading, and in a
multi-agent repo it propagates. Board: [../backlog.md](../backlog.md).

---

## SKL-26 — Refresh `AGENTS.MD` ownership map and module tree

**Type** Chore · **Priority** P2 · **Estimate** S · **Status** Todo

`AGENTS.MD` is the file every agent is instructed to read first, and three parts of it are wrong.

**1. The ownership map is stale.** These are all marked `⬜ Pending / Unassigned` but exist and are
complete: `compose.yml`, `Dockerfile`, `.github/workflows/ci.yml`, `.github/workflows/release.yml`,
`contract/openapi.yml`, `contract/asyncapi.yml`, the Grafana dashboard JSON, domain model classes,
application use cases, and infrastructure adapters.

**2. The repository tree omits two modules.** It lists six under `code/`; `boot` and `resilience`
are missing (though `boot` appears in the ownership map — internally inconsistent).

**3. The stated dependency direction does not match the POMs.** It claims
`infrastructure ──▶ application, domain, contract, logging, observability`. In reality
`code/infrastructure/pom.xml` does **not** declare `contract` — it arrives transitively via
`application`. Likewise `boot` declares neither `contract` nor `domain` directly. The verified graph
is in [../../architecture/module-topology.md](../../architecture/module-topology.md).

This matters beyond tidiness: an agent that trusts the documented graph will assume `contract` types
are directly available in `infrastructure` and write code that compiles today but breaks the moment
the `application` dependency changes.

**Acceptance**
- [ ] Ownership map reflects reality; completed items marked Done with an owner
- [ ] Module tree lists all eight modules
- [ ] Dependency direction matches the POMs, with transitive-vs-declared called out
- [ ] Cross-reference to `lode/architecture/module-topology.md` added
- [ ] Coordination protocol reconciled with the SKL-29 commit convention

**Related** SKL-29

---

## SKL-27 — Consolidate `KNOWLEDGE-BASE.md` and `CONFIGURATIONS.md` into the lode

**Type** Chore · **Priority** P3 · **Estimate** M · **Status** Todo

The lode now duplicates most of two root documents. Two copies of the same knowledge drift, and
drift is exactly what the lode exists to prevent.

| Root file | Overlaps with | Verdict |
|---|---|---|
| `KNOWLEDGE-BASE.md` (17 pitfalls + rationale) | `build/maven-conventions.md`, `infrastructure/database-migrations.md`, `local-dev/*`, `architecture/summary.md` | ~90% duplicated |
| `CONFIGURATIONS.md` (env var reference) | `local-dev/configuration.md` | ~100% duplicated, and already stale |
| `README.md` | — | Keep. Public-facing. |
| `CONTRIBUTING.md` | — | Keep. Human contributor workflow. |
| `AGENTS.MD` | `practices.md` | Keep, but see SKL-26 |
| `{MODEL}-TASKS.md` | — | Keep. Changelog-style by design, per `AGENTS.MD`. |

Already-detected drift: `KNOWLEDGE-BASE.md` §6 states `application-local.yml` is git-ignored
(false until SKL-2 lands); `CONFIGURATIONS.md` lists `SPRING_DATA_COUCHBASE_BUCKET_NAME` defaulting
to `skeletoni` while `application-test.yml` uses `test` and no default exists in base config.

Do not simply delete them — they are linked from `README.md` and referenced by `AGENTS.MD`.
Replace their bodies with short pointers into the lode, preserving the genuinely
human-facing troubleshooting commands.

**Acceptance**
- [ ] Every fact in both files verified against code, then either merged into the lode or discarded
- [ ] Root files reduced to pointers, or removed with all inbound links updated
- [ ] `README.md` links checked
- [ ] [../../lode-map.md](../../lode-map.md) §"Relationship to root documentation" updated
- [ ] No fact exists in two places afterwards

**Blocked by** SKL-2, SKL-26 (fix the falsehoods before deciding what to preserve)

---

## SKL-38 — `README.md` describes a system that does not exist

**Type** Bug · **Priority** P1 · **Estimate** M · **Status** **Done**

**Resolution.** README rewritten so every claim is labelled *Implemented* or *Planned*. Aspirational
content was preserved under an explicit "Planned — design intent, not yet built" heading rather than
deleted: the intended shape is genuinely useful to someone building on the skeleton, it just must
not read as a description of current state. A "What works today" table near the top gives the honest
summary in one screen.

Factual corrections made, each verified against the filesystem or config:

| Was | Is |
|---|---|
| gRPC on `localhost:9090`, env var `GRPC_PORT` | port `9091`, set via `grpc.server.port` in YAML — no env var |
| RabbitMQ `guest/guest` | `user` / `password` |
| `resources/openapi/openapi.yml`, `resources/asyncapi/asyncapi.yml` | both at `resources/` root, no subdirectories |
| `observability/dashboards/skeletoni-dashboard.json` covering Kafka lag, RabbitMQ depth, business metrics | `infra/grafana/dashboards/skeletoni-jvm.json`, JVM + HTTP built-ins only |
| "Every log line is emitted as JSON", `traceId`/`spanId` via Micrometer Tracing | plain-text console; Logstash appender defined but unattached; no tracing dependency |
| CI "on every push and PR to `main`" | every branch, every PR, plus `workflow_dispatch` |
| `release.yml` pushes a Docker image, needs registry secrets | builds no image; only `SONAR_TOKEN` and the automatic `GITHUB_TOKEN` |
| `SPRING_COUCHBASE_CONNECTION_STRING`, `MANAGEMENT_PROMETHEUS_ENABLED` | `SPRING_DATA_COUCHBASE_*`; the latter is not used at all |
| Virtual Threads / Project Loom, rate limiter, bulkhead, distributed tracing | removed — none exist in any form |

Warnings added where a reader would otherwise be misled: `mvn verify` does not execute `*IT`
classes (`SKL-32`), the Avro `.gitkeep` is load-bearing (`SKL-33`), `SPRING_DATASOURCE_*` must stay
out of CI (`SKL-35`), and empty package directories are reserved scaffolding rather than code.

**Correction to the original report below:** it was written against the copy on `origin/develop`.
The module tree on `feature/SKL-1` had already been partially fixed — it showed the `code/`
container and the correct eight modules. The file-level claims and everything outside the tree were
still wrong on both branches.

### Original report

The README's "Module Structure" section is a design document written before implementation and
presented as a description of the code. Twelve files it names were checked; **all twelve are
absent.** The entire project contains 23 Java files.

| README claims | Reality |
|---|---|
| `application/command/CreateExampleCommandHandler.java` | absent — no CQRS handlers exist |
| `application/query/GetExampleQuery.java` + handler | absent — no query side at all |
| `application/scheduler/ExampleScheduler.java` | absent — no `@Scheduled`, no `@EnableScheduling` |
| `application/port/out/ExampleEventPublisher.java` | absent (`SKL-6`) |
| `domain/service/ExampleDomainService.java` | absent |
| `infrastructure/kafka/`, `mongodb/`, `couchbase/`, `rabbitmq/`, `grpc/` adapters | none exist (`SKL-10`) |
| `infrastructure/config/{Kafka,RabbitMQ,Mongo,Scheduler}Config.java` | none exist |
| `logging/MdcContextFilter`, `CorrelationIdInterceptor`, `KafkaMdcConsumerInterceptor`, `LoggingAutoConfiguration` | only `CorrelationIdFilter` exists |
| `observability/dashboards/skeletoni-dashboard.json` | actually `infra/grafana/dashboards/skeletoni-jvm.json` |

Structural errors beyond missing files:

- The module tree places modules at the **repo root** (`skeletoni/contract/`). They live under
  `code/`, and the reactor root is `code/pom.xml`, not the repo root — the single most important
  fact about building this project.
- Resource paths are wrong: `contract/src/main/resources/openapi/openapi.yml` is actually
  `contract/src/main/resources/openapi.yml`. Same for `asyncapi/`.
- `logging` and `observability` are shown but `resilience` and `boot` are omitted entirely.

False claims elsewhere in the file:

- Tech stack lists "Virtual Threads / Project Loom" and "Scheduling: Spring `@Scheduled` +
  Virtual Thread executor" — no virtual thread configuration exists anywhere.
- Resilience4j is described as "Circuit Breaker, Retry, Rate Limiter, Bulkhead"; only circuit
  breaker and retry are configured, and neither is applied to any call site (`SKL-13`).
- Observability claims "Distributed Tracing"; there is no tracing dependency (`SKL-16`).
- The testing table claims `@WebMvcTest` controller slice tests and unit tests for `domain` and
  `application` — none exist (`SKL-18`, `SKL-19`).
- "Testcontainers manages real PostgreSQL, MongoDB, Kafka, and RabbitMQ containers" — only
  PostgreSQL, and that test does not execute (`SKL-32`).
- "SonarCloud analysis runs on every push to `main`" — `ci.yml` now triggers on all branches.

**Why this outranks most of the backlog.** This project exists to be cloned. The README is the first
and often only thing a user reads, and it currently promises a working CQRS/multi-protocol service.
Someone clones it, looks for `ExampleKafkaProducer`, and finds nothing. Every other gap in this
backlog is a missing feature; this one is a false claim about what was delivered.

Recommended approach: rewrite the module tree from the actual filesystem, and split every
aspirational claim into a clearly-labelled "Planned / not yet implemented" section rather than
deleting it — the design intent is genuinely useful, it just must not masquerade as current state.
`lode/` now holds the verified structure and can be used as the source.

**Acceptance**
- [ ] Module tree generated from the real filesystem, with modules under `code/`
- [ ] Every named file in the README verified to exist
- [ ] Reactor root (`code/pom.xml`) stated explicitly
- [ ] Unimplemented capabilities moved under an explicit "Planned" heading
- [ ] Tech stack table claims only what is wired: no virtual threads, no tracing, no rate
      limiter/bulkhead
- [ ] Testing section matches `lode/testing/summary.md`

**Related** SKL-26 (`AGENTS.MD` drift), SKL-27 (consolidate root docs), SKL-10

---

## Standing rule

Closing any ticket includes updating affected lode files **in the same change**. A ticket whose lode
is stale is not Done. This is the mechanism that keeps this epic from regenerating itself.

---

Related: [../known-gaps.md](../known-gaps.md) entries 9, 10
