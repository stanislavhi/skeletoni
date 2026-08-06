# Known Gaps

Current divergences between what skeletoni advertises and what it does. Each entry states the gap,
the impact, and the fix. Remove an entry when it is closed — this is state, not history.

Tickets: [backlog.md](backlog.md). Entries are deleted from this file when their ticket closes —
this describes present reality, not resolved history.

## Blocking / correctness

### 0. Integration tests never execute — no failsafe plugin · `SKL-32`
`mvn verify` is green and reports `Tests run: 1`. Only `SkeletoniApplicationTests` runs;
`ExamplePostgresAdapterIT` compiles and is silently skipped, because `maven-failsafe-plugin` is not
configured and surefire's default includes do not match the `*IT` suffix. Every integration test
this project writes is dead code until this is fixed — and the suite reports success while doing it.
The IT's three assertions have therefore never been proven against a real database.

### 1. `spring.cloud.stream.function.definition: exampleConsumer` has no bean · `SKL-3`
`application.yml` binds `exampleConsumer-in-0` to `example-topic`, but no `Consumer<...>` bean
exists anywhere. Spring Cloud Stream cannot resolve the function definition at runtime.
**Fix:** implement the consumer in `infrastructure`, or remove the `spring.cloud.stream` block
until messaging is real. See [../contract/messaging-and-grpc.md](../contract/messaging-and-grpc.md).

### 3. No transaction boundaries · `SKL-4`
`ExampleService.createExample` is not `@Transactional`. Correct only because it performs exactly
one write. Adding an event publish or a second repository call silently introduces partial-write
bugs. **Fix:** annotate use-case methods before extending them.

### 4. No input validation · `SKL-5`
`CreateExampleRequest.name` is `required` in `openapi.yml` but has no `@NotBlank`, the controller
has no `@Valid`, and `Example.create` guards nothing. A null name reaches the database and fails on
the `NOT NULL` constraint as a 500 rather than a 400.
**Fix:** validate in the domain factory; add `@Valid` for the HTTP-level 400.

## Declared but unimplemented

| Feature | Present | Missing |
|---|---|---|
| gRPC | `.proto`, codegen, starter, `grpc.server.port: 9091` | any `@GrpcService` impl |
| Kafka produce | starter, binder, serializers, `example-topic` in AsyncAPI | `ExampleEventPublisher` port + adapter |
| Kafka consume | binder + binding config | `exampleConsumer` bean |
| RabbitMQ | starter, binder, credentials | any listener or publisher |
| MongoDB | starter, compose service, URI | any `ExampleDocument` / adapter |
| Couchbase | starter, compose service, credentials | any adapter |
| Avro | plugin, confluent serializer, Schema Registry | empty `contract/src/main/resources/avro/` |
| Resilience4j | full instance config + event logging | no `@CircuitBreaker` / `@Retry` call site (`SKL-13`) |
| Domain events | `ExampleCreatedEvent` record + AsyncAPI schema | never constructed or published (`SKL-6`) |
| CQRS handlers | `CreateExampleCommand` | no command/query handlers, no query objects |

Cost of the unimplemented starters is not zero: each one keeps its auto-configuration active, which
is why `application.yml` demands Mongo/Couchbase/Rabbit credentials and `application-test.yml` must
exclude four auto-configurations and supply dummy placeholders.

## Configuration inconsistencies

### 6. Logstash appender is dead config · `SKL-12`
`logback-spring.xml` defines a `LogstashTcpSocketAppender` to `localhost:5044` that the root logger
never references, and no Logstash service exists in `compose.yml`. Either add the service and
attach the appender behind a `<springProfile>`, or delete the appender.
See [../observability/correlation-id.md](../observability/correlation-id.md).

### 7. Kafka is configured for JSON, tooled for Avro · `SKL-9`
`JsonSerializer` / `ErrorHandlingDeserializer` in `application.yml` alongside Avro plugin, Confluent
serializer and a running Schema Registry. Pick one.

## Documentation drift

`README.md` was the worst offender here and has been rewritten (`SKL-38`). It now labels every
capability *Implemented* or *Planned*. `AGENTS.MD` and the remaining root docs have not.

### 9. `AGENTS.MD` ownership map is stale · `SKL-26`
`compose.yml`, `Dockerfile`, `ci.yml`, `release.yml`, `openapi.yml`, `asyncapi.yml` and the Grafana
dashboard are all marked "⬜ Pending / Unassigned" but exist and are complete.

### 10. `AGENTS.MD` structure primer omits two modules
The repository tree in that file lists six modules; `boot` and `resilience` are missing from the
tree (though `boot` appears in the ownership map). Its stated dependency direction also lists
`infrastructure → contract`, which is only transitive in the real POMs. Actual graph:
[../architecture/module-topology.md](../architecture/module-topology.md).

## Testing

### 11. Only two tests exist, and one does not compile · `SKL-18`, `SKL-31`
`SkeletoniApplicationTests` (context load) and `ExamplePostgresAdapterIT` — the latter blocked by
gap 0. Everything else is untested despite the mandatory-tests rule. Gap list in
[../testing/summary.md](../testing/summary.md).

### 12. Nothing verifies controller ⟷ `openapi.yml` agreement · `SKL-20`
The controller is hand-written; Spectral only validates that the YAML is well-formed. They can
drift silently. Generating the controller from the spec would remove the class of bug — see the
trade-off note in [../contract/rest-delegate-pattern.md](../contract/rest-delegate-pattern.md).

### 13. CI service containers have no health gate · `SKL-21`
Maven can start before Postgres/Kafka are ready. Testcontainers is the more reliable path.

## Build & release

### 14. Every new Maven module needs a manual `COPY` line in the Dockerfile · `SKL-23`
The jar name is now a glob, but `dependency:go-offline` still requires one explicit
`COPY code/<module>/pom.xml` per module. Adding a ninth module and forgetting the line yields an
incomplete reactor. Now commented in the Dockerfile; the checklist lives in
[../build/maven-conventions.md](../build/maven-conventions.md).

### 15. Release builds skip tests, and no image is ever published · `SKL-24`
`release.yml` runs `package -DskipTests`, so a tag can ship a jar CI never validated — currently
including a test suite that does not compile (gap 0). Separately, `ci.yml`'s `build-docker` uses
`push: false` and no workflow publishes an image.

### 16. No release has ever been cut · `SKL-30`
`git tag -l` is empty, the version is still `0.0.1-SNAPSHOT`, and the changelog's `[Unreleased]`
section has accumulated everything. The release path is untested in practice. See
[../build/versioning-and-releases.md](../build/versioning-and-releases.md).

### 17. Two competing commit conventions · `SKL-29`
`AGENTS.MD` mandates `[actor]` prefixes; history also contains Conventional Commits
(`feat:`, `fix: fixes 11`). Neither is applied consistently, which blocks automated changelog and
version inference.

### 18. `CHANGELOG.md` is at `code/`, not the repo root · `SKL-28`
GitHub does not surface it there, and it reads as documentation of the `code` module.

Related: [backlog.md](backlog.md)
