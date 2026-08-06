# Practices & Invariants

Rules that hold across skeletoni. Violating any of these is a defect, not a style preference.

## Hard invariants

1. **Dependency direction is one-way.** `domain` and `contract` and `logging` and `observability`
   and `resilience` depend on no internal module. `application` → `contract`, `domain`.
   `infrastructure` → `application`, `domain`, `logging`, `observability`. `boot` → everything.
   See [architecture/module-topology.md](architecture/module-topology.md).
2. **`domain` imports no framework.** No Spring, no JPA, no Jackson, no Kafka. Only JDK + Lombok.
   `code/domain/pom.xml` declares zero dependencies — keep it that way.
3. **`application` never imports an adapter.** It talks to out-ports only.
4. **MapStruct mappers live in `infrastructure`.** Never in `domain` or `application`.
5. **No hardcoded config in Java.** Everything externalised to `application.yml`.
   Cron expressions go under `app.scheduler.*`.
6. **No secrets in committed code.** Use env vars / `application-local.yml`.
   *Currently violated* — see [plans/known-gaps.md](plans/known-gaps.md).
7. **Every new class gets a test.** Unit tests in the owning module; Testcontainers integration
   tests in `code/infrastructure/src/test`, suffixed `IT`.

## Java style

- Java 21 idioms only. `Stream.toList()` — never `collect(Collectors.toList())` (Sonar `java:S6204`).
- `record` for DTOs, value objects and events; Lombok `@Value` / `@Builder` for aggregates needing builders.
- Lombok is available in every module: prefer `@RequiredArgsConstructor` for injection, `@Slf4j` for logging.
- Constructor injection only. No field `@Autowired`.

```java
// application layer: command in, domain out, no framework leakage downward
@Service
@RequiredArgsConstructor
public class ExampleService implements ExampleUseCase {
  private final ExampleRepository exampleRepository;   // out-port, not an adapter

  @Override
  public List<Example> listExamples() {
    return exampleRepository.findAll();               // Stream.toList() downstream
  }
}
```

## Contract-first

- `openapi.yml`, `asyncapi.yml` and `proto/example.proto` in `code/contract/src/main/resources`
  are the source of truth for external communication. Change the contract before the code.
- CI lints both YAML contracts with Spectral — a malformed contract fails the build.
- DTOs are owned exclusively by `contract`. Never redeclare them elsewhere.

## Build & tooling

- Approved stack only (`AGENTS.MD`): Maven, Spring Boot 4, Kafka/RabbitMQ, Postgres/Mongo/Couchbase,
  Flyway, Resilience4j, MapStruct, Lombok, SLF4J+Logback, JUnit 5 + Mockito + Testcontainers,
  SpringDoc OpenAPI 3, AsyncAPI 3. H2 is permitted **in tests only**.
- Flyway pinned to **9.22.3** deliberately — 10.x splits DB support into separate artifacts that
  resolve badly through the Confluent mirror. Do not "upgrade" without re-testing resolution.
- Annotation processor order in the parent POM matters: `mapstruct-processor`, `lombok`,
  `lombok-mapstruct-binding`. Removing the binding breaks generated mappers.
- MapStruct custom `expression` mappings need explicit `imports` on `@Mapper` or the generated
  impl fails with `cannot find symbol`.

## Multi-agent protocol

- Read `AGENTS.MD` and the `*-TASKS.md` files before starting work.
- Log every created/modified file and every decision in your own `{MODEL}-TASKS.md`.
- Commit messages are prefixed with the actor: `[claude]`, `[gemini]`, `[antigravity]`, `[human]`.
- Never delete another agent's files unilaterally; deprecate and flag instead.

## Lode discipline

- This lode is the durable memory. Session scraps go to `lode/tmp/` (git-ignored).
- Lode files describe **current state**, never change history.
- When code and lode disagree, the code wins — fix the lode and say so.
- Work is tracked as `SKL-` tickets on [plans/backlog.md](plans/backlog.md). A ticket is not Done
  until the lode files it touches are updated **in the same change**.
- **Check `git status` before committing lode changes.** `.gitignore` carries broad IDE rules —
  `build/` (NetBeans) matches `lode/build/` at any depth, and `bin/`, `out/`, `target/` would do the
  same. A `!lode/build/` negation exists for that reason. A new lode directory whose name collides
  with one of those rules will be silently dropped from commits, with no warning.

Related: [build/maven-conventions.md](build/maven-conventions.md), [testing/summary.md](testing/summary.md)
