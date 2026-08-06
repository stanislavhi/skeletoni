# Epic SKL-CORE — Correctness & Invariant Violations

Things that are outright wrong: a broken invariant, a latent runtime failure, or a missing
guarantee. Board: [../backlog.md](../backlog.md).

---

## SKL-2 — Untrack `application-local.yml` from the git index

**Type** Bug · **Priority** P0 · **Estimate** XS · **Status** Todo

`.gitignore` line 44 lists `**/application-local.yml` and `KNOWLEDGE-BASE.md` states the file is
git-ignored. Both are false: `git ls-files` shows
`code/boot/src/main/resources/application-local.yml` in the index. It was committed before the
ignore rule was added, and `.gitignore` has no effect on already-tracked paths. This makes the
"no secrets in code" invariant in [../../practices.md](../../practices.md) untrue.

Exposure is low — the values are Docker Compose defaults — but the mechanism is the bug, not the
values. The next developer who puts a real credential there will have it committed silently.

```bash
git rm --cached code/boot/src/main/resources/application-local.yml
```

Keep `application-local.yml.example` tracked; that one is intentional.

**Acceptance**
- [ ] `git ls-files | grep application-local.yml` returns only the `.example` file
- [ ] The file still exists on disk and the app still boots on the `local` profile
- [ ] `KNOWLEDGE-BASE.md` §6 and `CONFIGURATIONS.md` no longer contradict reality
- [ ] Sonar exclusions in `code/pom.xml` and `sonar-project.properties` still cover the `.example`

**Lode to update** [../../local-dev/configuration.md](../../local-dev/configuration.md) §"The
`application-local.yml` problem"

---

## SKL-3 — Resolve the `exampleConsumer` binding that has no bean

**Type** Bug · **Priority** P0 · **Estimate** S · **Status** Todo

`application.yml` declares:

```yaml
spring.cloud.stream:
  function.definition: exampleConsumer
  bindings:
    exampleConsumer-in-0:
      destination: example-topic
      group: skeletoni-group
```

No `exampleConsumer` bean exists anywhere in the codebase. Spring Cloud Stream cannot resolve the
function definition. This is currently masked because `application-test.yml` sets
`spring.cloud.stream.enabled: false` and no environment actually runs the stream binder — so the
failure is latent, waiting for the first real boot with Kafka reachable.

Two valid resolutions; pick one and record the decision here:

- **(a)** Implement the consumer — becomes SKL-7, which this ticket then blocks on.
- **(b)** Delete the `spring.cloud.stream` block until messaging is real. Cheaper, honest, and
  consistent with SKL-10's direction.

Recommendation: **(b) now, (a) later.** Configuration that describes non-existent code is worse
than absent configuration, and SKL-6 will reintroduce the block deliberately.

**Acceptance**
- [ ] Either an `exampleConsumer` bean exists, or the `spring.cloud.stream` block is removed
- [ ] Application boots against a live Kafka on the `local` profile with no binder errors
- [ ] Decision (a or b) recorded in this ticket and in
      [../../contract/messaging-and-grpc.md](../../contract/messaging-and-grpc.md)

**Blocks** SKL-7

---

## SKL-4 — Add transaction boundaries to use-case methods

**Type** Bug · **Priority** P1 · **Estimate** S · **Status** Todo

`ExampleService.createExample` performs exactly one repository write and is not `@Transactional`.
It is atomic by accident. The moment SKL-6 adds an event publish — or any second write — a failure
between the two leaves the system in a partial state with no rollback.

```java
@Transactional
@Override
public Example createExample(CreateExampleCommand command) { ... }

@Transactional(readOnly = true)
@Override
public List<Example> listExamples() { ... }
```

Design question to settle while doing this, because it determines the shape of SKL-6: publishing to
Kafka inside a JPA transaction does **not** make the publish transactional. The options are
transactional outbox, or `@TransactionalEventListener(phase = AFTER_COMMIT)`, or accepting
at-least-once with an idempotent consumer. For a skeleton, `AFTER_COMMIT` is the honest default —
document the at-least-once semantics rather than implying exactly-once.

**Acceptance**
- [ ] Write use cases are `@Transactional`; read use cases are `@Transactional(readOnly = true)`
- [ ] The event-publish-vs-transaction decision is documented in
      [../../application/summary.md](../../application/summary.md)
- [ ] An integration test proves rollback on failure after the write

**Blocked by** SKL-6 (do them together — the boundary only matters once there are two operations)

---

## SKL-5 — Add input validation across the slice

**Type** Bug · **Priority** P1 · **Estimate** S · **Status** Todo

`CreateExampleRequest.name` is marked `required` in `openapi.yml`, but nothing enforces it:
the DTO has no constraint annotation, `ExampleController.createExample` has no `@Valid`, and
`Example.create(name)` accepts null. A null name travels the full stack and dies on the
`name VARCHAR(255) NOT NULL` column as a 500 that should have been a 400.

Validation belongs in two places, for two different reasons:

1. **Domain** (`Example.create`) — the aggregate must be incapable of existing in an invalid state,
   regardless of which port created it. This is the real invariant.
2. **Edge** (`@Valid` + `@NotBlank`) — so HTTP clients get a 400 with a field-level message instead
   of a domain exception surfacing as a 500.

Note `contract` has `spring-boot-starter-web` but validation needs
`spring-boot-starter-validation` — check whether it arrives transitively before assuming it.

**Acceptance**
- [ ] `@NotBlank` on `CreateExampleRequest.name`, `@Valid` on the controller parameter
- [ ] `Example.create` rejects null/blank names with a domain exception
- [ ] `@RestControllerAdvice` maps validation failures to RFC 7807 `ProblemDetail` (Boot 4 default)
- [ ] Tests cover: blank name → 400 with field detail; direct `Example.create(null)` → throws
- [ ] `openapi.yml` documents the 400 response

**Blocks** SKL-19

---

## SKL-31 — `ExamplePostgresAdapterIT` does not compile; the build is red

**Type** Bug · **Priority** P0 · **Estimate** S · **Status** **Done**

**Resolution.** Spring Boot 4 split the sliced-test annotations out of `spring-boot-starter-test`
into per-technology starters, and moved their packages:

| Annotation | Boot 3 package | Boot 4 package | Boot 4 starter |
|---|---|---|---|
| `@DataJpaTest` | `org.springframework.boot.test.autoconfigure.orm.jpa` | `org.springframework.boot.data.jpa.test.autoconfigure` | `spring-boot-starter-data-jpa-test` |
| `@AutoConfigureTestDatabase` | `org.springframework.boot.test.autoconfigure.jdbc` | `org.springframework.boot.jdbc.test.autoconfigure` | `spring-boot-starter-jdbc-test` |

Both starters are managed by `spring-boot-dependencies:4.0.3` — no explicit version. Added
test-scoped to `code/infrastructure/pom.xml` with a comment, and the two imports updated.
`mvn -B clean verify -f code/pom.xml` is now **BUILD SUCCESS** across all nine modules.

Same family of relocation as `MeterRegistryCustomizer` and `Health`/`HealthIndicator`; the full
table lives in [../../build/maven-conventions.md](../../build/maven-conventions.md). Other slices
follow the pattern — `spring-boot-starter-webmvc-test` carries `@WebMvcTest` for SKL-19.

**Caveat:** the IT compiles but still does not *execute* — see SKL-32.

### Original report

`mvn -B clean verify -f code/pom.xml` **fails** at `infrastructure:testCompile`. The project's only
integration test has never compiled:

```
ExamplePostgresAdapterIT.java:[11,56] cannot find symbol
  symbol:   class AutoConfigureTestDatabase
  location: package org.springframework.boot.test.autoconfigure.jdbc
ExamplePostgresAdapterIT.java:[12,59] package org.springframework.boot.test.autoconfigure.orm.jpa does not exist
ExamplePostgresAdapterIT.java:[24,2] cannot find symbol
  symbol: class DataJpaTest
```

`code/infrastructure/pom.xml` declares `spring-boot-starter-test` and `spring-boot-testcontainers`,
so this is not a plain missing-dependency mistake. **Spring Boot 4 relocated the sliced-test
annotations**: `@DataJpaTest` and `@AutoConfigureTestDatabase` are no longer reachable from
`spring-boot-starter-test` at their Boot 3 packages. This is the same modularization already
observed elsewhere in the codebase — `MeterRegistryCustomizer` moved to
`org.springframework.boot.micrometer.metrics.autoconfigure`, and `Health`/`HealthIndicator` to
`org.springframework.boot.health.contributor` (see
[../../observability/metrics-and-health.md](../../observability/metrics-and-health.md)).

Do not guess the replacement artifact. Determine it from the Boot 4.0.3 dependency tree / release
notes, then decide between:

- **(a)** Add the correct Boot 4 test-slice artifact and keep `@DataJpaTest`.
- **(b)** Drop the slice: use `@SpringBootTest` with `@ServiceConnection` on the container, which
  Boot 4 favours and which would also serve SKL-21.

`-DskipTests` builds succeed, which is exactly why this went unnoticed — and why SKL-24 matters:
`release.yml` uses `-DskipTests`, so this branch could be tagged and shipped today with a
non-compiling test suite.

**Acceptance**
- [x] `mvn -B clean verify -f code/pom.xml` is green with tests enabled
- [x] The Boot 4 import paths recorded in
      [../../build/maven-conventions.md](../../build/maven-conventions.md)
- [x] [../../testing/summary.md](../../testing/summary.md) corrected
- [ ] ~~IT's three test methods pass against Testcontainers~~ → moved to SKL-32; the test does not
      execute at all yet

**Discovered** while executing SKL-2/11/22/23/25. The file is part of the untracked
`code/infrastructure/src/test/` tree on `feature/SKL-1` — pre-existing, unrelated to those edits.

---

## SKL-32 — Integration tests never execute: no failsafe plugin

**Type** Bug · **Priority** P0 · **Estimate** S · **Status** Todo

With SKL-31 fixed, `mvn verify` is green — and reports **`Tests run: 1`**. The only test that ran is
`SkeletoniApplicationTests`. `ExamplePostgresAdapterIT` compiles and is silently ignored.

Cause: `maven-failsafe-plugin` is **not configured anywhere** in the build. `maven-surefire-plugin`
runs unit tests and its default includes are `**/Test*.java`, `**/*Test.java`, `**/*Tests.java`,
`**/*TestCase.java`. The `*IT` suffix is Failsafe's convention, and Failsafe is absent — so every
integration test this project ever writes is dead code by default.

This is more dangerous than the compile error it was hiding behind. A compile error is loud; a
silently skipped test suite reports success. The convention documented in
[../../testing/summary.md](../../testing/summary.md) ("integration tests are suffixed `IT`") is
actively harmful until this is fixed.

Add to `code/pom.xml`'s `<pluginManagement>` and activate in `infrastructure`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <executions>
    <execution>
      <goals><goal>integration-test</goal><goal>verify</goal></goals>
    </execution>
  </executions>
</plugin>
```

Note Failsafe binds to `integration-test`/`verify`, so `mvn test` stays fast and `mvn verify` runs
the slow container-backed suite — which is the split the CI/local story wants anyway.

Verification requires Docker; it was unavailable on the machine where SKL-31 was fixed, so the IT's
three assertions remain **unproven**. CI runners have Docker, so the first green CI run after this
lands is the real proof.

**Acceptance**
- [ ] `maven-failsafe-plugin` configured; `mvn verify` reports the IT's 3 tests as run
- [ ] `mvn test` does **not** run `*IT` classes
- [ ] A deliberately failing assertion in the IT fails the build (proves it is wired, not skipped)
- [ ] Confirmed green in CI, where Docker is available
- [ ] [../../testing/summary.md](../../testing/summary.md) and
      [../../build/maven-conventions.md](../../build/maven-conventions.md) updated

**Blocks** SKL-18, SKL-21

---

Related: [../known-gaps.md](../known-gaps.md) entries 0, 1, 3, 4, 11
