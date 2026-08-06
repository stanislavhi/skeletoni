# Epic SKL-PLAT — Build, Config & Release

Build hygiene, configuration correctness, and the release path — which has never actually run.
Board: [../backlog.md](../backlog.md).

---

## SKL-22 — Move dev-only settings into the `local` profile

**Type** Chore · **Priority** P2 · **Estimate** XS · **Status** Todo

Two development conveniences sit in base `application.yml`, so they apply to every profile
including production:

```yaml
spring.jpa.show-sql: true                      # logs every statement
management.endpoint.health.show-details: always # exposes internals unauthenticated
```

`show-sql` is a performance and log-volume problem. `show-details: always` is information
disclosure — it exposes datastore names, versions and (once SKL-13 lands) circuit-breaker state to
anyone who can reach `/actuator/health`.

Move `show-sql` to `application-local.yml`; set `show-details: when-authorized` in base and
`always` in `local`.

**Acceptance**
- [ ] Base `application.yml` contains neither dev setting
- [ ] `local` profile retains both; behaviour unchanged for developers
- [ ] Non-local profile returns health without detail

---

## SKL-23 — Parameterise the jar name in the Dockerfile

**Type** Chore · **Priority** P2 · **Estimate** XS · **Status** Todo

```dockerfile
COPY --from=build /app/code/boot/target/boot-0.0.1-SNAPSHOT.jar app.jar
```

Hardcoded version. The first `mvn versions:set` — which SKL-30 requires — breaks the image build
with a confusing `file not found`. Fix with a glob or a build arg:

```dockerfile
COPY --from=build /app/code/boot/target/boot-*.jar app.jar
```

Second latent issue in the same file: every module needs an explicit
`COPY code/<module>/pom.xml code/<module>/` line before `dependency:go-offline`. All eight are
currently listed, but adding a ninth module and forgetting the line yields an incomplete reactor.
Add a comment marking it, and cover it in the checklist in
[../../build/maven-conventions.md](../../build/maven-conventions.md).

**Acceptance**
- [ ] Image builds after an arbitrary version change
- [ ] `docker build .` succeeds and the container starts
- [ ] Module-COPY requirement commented in the Dockerfile

**Blocks** SKL-30

---

## SKL-24 — Stop skipping tests on release; publish the image

**Type** Task · **Priority** P2 · **Estimate** S · **Status** Todo

`release.yml` runs `mvn -B clean package -DskipTests`. Tagging a commit that CI never validated
ships an unverified jar, and nothing enforces that the tagged commit passed CI. Separately,
`build-docker` in `ci.yml` builds with `push: false` — **no workflow ever publishes an image**, so
the Dockerfile is validated but the artifact is unreachable.

**Acceptance**
- [ ] Release either runs `verify` or requires a green CI run on the tagged commit
- [ ] Image published to GHCR on tag, tagged with both the version and `latest`
- [ ] Jar and image carry the same version
- [ ] [../../build/ci-cd.md](../../build/ci-cd.md) updated

**Blocked by** SKL-30

---

## SKL-25 — Implement or remove `app.scheduler.example-cron`

**Type** Chore · **Priority** P3 · **Estimate** XS · **Status** Todo

`application.yml` defines `app.scheduler.example-cron: "0 * * * * *"` (every minute). There is no
`@Scheduled` method and no `@EnableScheduling` anywhere — the property is read by nothing.
`AGENTS.MD` mandates that cron expressions live under `app.scheduler.*`, so the convention is
established; only the consumer is missing.

Either add a demonstration scheduled component (in `application`, per the module decision tree) or
delete the property. If added, note that scheduled threads have no correlation ID until SKL-14.

**Acceptance**
- [ ] Either a `@Scheduled(cron = "${app.scheduler.example-cron}")` component exists with
      `@EnableScheduling`, or the property is removed
- [ ] If added: covered by a test and noted as an MDC gap for SKL-14

---

## SKL-33 — Keep the Avro source directory present in a fresh clone

**Type** Bug · **Priority** P0 · **Estimate** XS · **Status** **Done**

`avro-maven-plugin` is bound to `generate-sources` and fails outright when neither
`src/main/resources/avro` nor `src/test/avro` is a directory:

```
Failed to execute goal org.apache.avro:avro-maven-plugin:1.12.0:schema (default) on project
contract: neither sourceDirectory .../code/contract/src/main/resources/avro or
testSourceDirectory .../code/contract/src/test/avro are directories
```

Git does not track empty directories, so the directory existed on every developer machine and in
**no** CI clone. The build passed locally and failed in CI at the second reactor module, marking the
other seven `SKLIPPED` — meaning **no test had ever run in CI**.

This is the general trap: an empty directory that a build plugin requires is invisible to git.
Resolved with a `.gitkeep` carrying an explanatory comment. Remove it once real `.avsc` schemas
exist, or once SKL-9 decides against Avro and the plugin goes.

**Acceptance**
- [x] `code/contract/src/main/resources/avro/.gitkeep` tracked
- [x] `contract` module builds from a clean clone
- [x] [../../contract/summary.md](../../contract/summary.md) records why the file is load-bearing

---

## SKL-34 — Add the Spectral ruleset the lint job requires

**Type** Bug · **Priority** P0 · **Estimate** XS · **Status** **Done**

The `lint-contracts` job ran `spectral lint` with no ruleset present, and the Spectral CLI has no
implicit default:

```
No ruleset has been found. Please provide a ruleset using the --ruleset CLI argument, or make
sure your ruleset file matches .?spectral.(js|ya?ml|json)
```

Exit code 2 on the first lint step. The job had **never linted anything** — it failed before
evaluating either spec, so the contract-first guarantee CI appeared to provide did not exist.

Added `.spectral.yaml` at the repo root extending built-in `spectral:oas` and `spectral:asyncapi`,
with four style rules disabled as noise for skeleton example specs. Spectral fails only on errors;
both contracts now emit warnings only and exit 0.

**Acceptance**
- [x] `.spectral.yaml` at repo root; both specs lint with exit 0
- [x] `lint-contracts` green in CI
- [x] [../../build/ci-cd.md](../../build/ci-cd.md) corrected — it previously claimed Spectral
      "applies its default rulesets", which is false

---

## SKL-35 — Stop overriding the test datasource from CI env vars

**Type** Bug · **Priority** P0 · **Estimate** XS · **Status** **Done**

`ci.yml` exported `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` into the Maven step. Environment
variables outrank profile YAML in Spring Boot's property source order, so the Postgres url replaced
the H2 url from `application-test.yml` — while `driver-class-name: org.h2.Driver` stayed, because no
env var overrides it:

```
Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:postgresql://localhost:5432/skeletoni
  -> Unable to determine Dialect without JDBC metadata
  -> SkeletoniApplicationTests.contextLoads fails, boot module fails
```

Reproduced locally by exporting the same three variables — **not** a Postgres readiness race, which
was the obvious-looking but wrong diagnosis given `KNOWLEDGE-BASE.md` §17 documents exactly such a
race. The env vars are removed, with a comment explaining why they must not return.

General lesson: partial property overrides are more dangerous than none. Overriding a `url` without
its `driver-class-name` produces a self-inconsistent datasource.

**Acceptance**
- [x] `SPRING_DATASOURCE_*` removed from `ci.yml`, with a comment preventing reintroduction
- [x] Root cause reproduced locally before changing anything
- [x] [../../build/ci-cd.md](../../build/ci-cd.md) documents the constraint

---

## SKL-36 — CI resolves the whole dependency tree twice per run

**Type** Task · **Priority** P2 · **Estimate** S · **Status** Todo

Measured on a real run: **509 artifact downloads before the build even cleared the `contract`
module.** Three compounding causes.

**1. The dependency tree is oversized.** Resolved dependency counts:

| Module | Deps |
|---|---|
| `domain` | 1 |
| `observability` | 40 |
| `logging` | 43 |
| `application` | 49 |
| `resilience` | 51 |
| `contract` | 70 |
| **`infrastructure`** | **307** |
| `boot` | 308 |

`infrastructure` contains five classes. The other 300-odd dependencies are Mongo, Couchbase,
RabbitMQ, Kafka, gRPC, Avro, Confluent and six Testcontainers modules with no code behind them.
**SKL-10 is the fix**, and this is its concrete cost.

**2. The Maven cache key is fragile.** `actions/setup-java` with `cache: 'maven'` keys on
`hashFiles('**/pom.xml')`. Any pom edit invalidates it, so active development on module POMs means
a cold cache on nearly every push.

**3. `build-docker` re-resolves everything.** The Dockerfile runs `dependency:go-offline` inside the
image build. Its only cache is `type=gha` docker layers, and the poms are `COPY`ed *before*
`go-offline` — so the same pom edit that busts the Maven cache also busts the layer. The result is
two full downloads per run in the same workflow.

Cause 3 is the cheapest to fix and the most clearly wasteful: `build-docker` rebuilds the
application from source purely to validate the Dockerfile, duplicating work the `build` job already
did. Options: pass the jar from `build` as an artifact and have the runtime stage consume it, or
give `build-docker` its own Maven cache, or drop the job and build the image only on release
(pairs with SKL-24, which currently publishes no image at all).

**Acceptance**
- [ ] A pom-only change no longer triggers two full dependency resolutions in one run
- [ ] `build-docker` either reuses the `build` job's artifact or is folded into the release flow
- [ ] Wall-clock time for a warm-cache run recorded here as a baseline
- [ ] [../../build/ci-cd.md](../../build/ci-cd.md) updated

**Related** SKL-10 (the root cause of the volume), SKL-24 (image publishing)

---

## SKL-37 — Remove the Maven wrapper

**Type** Chore · **Priority** P2 · **Estimate** XS · **Status** **Done**

The wrapper was already half-deleted: `mvnw` and `mvnw.cmd` were gone, leaving an orphaned
`.mvn/wrapper/maven-wrapper.properties` that could not run on its own. Nothing invoked it —
`ci.yml` and the `Dockerfile` both call plain `mvn` — while `README.md` and `CONTRIBUTING.md`
instructed contributors to run `./mvnw`, which would have failed immediately on a fresh clone.

Removed `.mvn/` entirely and dropped the `!.mvn/wrapper/maven-wrapper.jar` negation from
`.gitignore`. That negation was doubly ineffective: the jar was never tracked, and git cannot
re-include a file whose parent directory is excluded by the later `.mvn/` rule.

Two documentation bugs fixed in passing, both exposed by rewriting the commands:

- `./mvnw spring-boot:run -pl infrastructure` named a module with no `spring-boot-maven-plugin`
  and no main class. The runnable module is `boot`: `mvn spring-boot:run -f code/boot/pom.xml`.
- `mvn verify` was described as running integration tests. It does not — `maven-failsafe-plugin`
  is unconfigured (`SKL-32`).

All commands now use plain `mvn` with an explicit `-f`, since the reactor root is `code/pom.xml`
rather than the repo root.

**Trade-off accepted:** contributors must install Maven 3.9+ themselves; there is no pinned build
tool version. For a skeleton that is reasonable — CI pins the JDK via `setup-java`, and the wrapper
was providing no reproducibility because nothing used it.

**Acceptance**
- [x] No `mvnw`, `mvnw.cmd`, or `.mvn/` tracked; `.gitignore` negation removed
- [x] No `./mvnw` references remain in any root document
- [x] `mvn -B clean verify -f code/pom.xml` green after removal

---

## SKL-28 — Move `CHANGELOG.md` to the repo root

**Type** Chore · **Priority** P2 · **Estimate** XS · **Status** Todo

The changelog lives at `code/CHANGELOG.md` — inside the Maven reactor directory rather than at the
project root where every tool and human looks for it. GitHub does not surface it, and it reads as
documentation of the `code` module rather than of skeletoni.

Its content is sound: Keep a Changelog 1.0.0 format, SemVer declared, an `[Unreleased]` section, and
`SKL-1` already used as a ticket reference — which is what this backlog's numbering continues from.

**Acceptance**
- [ ] `CHANGELOG.md` at repo root (`git mv`, preserving history); no copy left in `code/`
- [ ] Links from `README.md` and `CONTRIBUTING.md`
- [ ] [../../build/versioning-and-releases.md](../../build/versioning-and-releases.md) updated

---

## SKL-29 — Settle the commit message convention

**Type** Chore · **Priority** P2 · **Estimate** S · **Status** Todo

The repository uses two incompatible conventions simultaneously:

| Style | Example | Mandated by |
|---|---|---|
| Actor prefix | `[claude] fix ci triggers and add docker build step` | `AGENTS.MD` coordination protocol |
| Conventional Commits | `feat: add Boot and Resilience Modules`, `fix: fixes 11` | git history only |

Neither is applied consistently, and `fix: fixes 11` carries no information at all. This blocks any
automated changelog or version inference, because a tool cannot infer a SemVer bump from `[claude]`
prefixes, and cannot attribute work from `feat:` alone.

The two are combinable — actor and semantic type are orthogonal:

```
feat(messaging): publish ExampleCreatedEvent [claude] SKL-6
fix(logging): clear MDC when filter chain throws [human] SKL-18
```

Type drives the SemVer bump, scope maps to the module, the actor tag preserves the `AGENTS.MD`
audit trail, and the ticket ID links back to this backlog.

**Acceptance**
- [ ] Convention decided and documented in `CONTRIBUTING.md` and `AGENTS.MD`
- [ ] `AGENTS.MD` coordination protocol updated so it no longer conflicts
- [ ] Optional: commit-msg hook or CI lint enforcing the format
- [ ] [../../practices.md](../../practices.md) §"Multi-agent protocol" updated

**Blocks** SKL-30

---

## SKL-30 — Cut `v0.1.0` and establish the tag → release flow

**Type** Task · **Priority** P2 · **Estimate** S · **Status** Todo

The release path has never executed. Evidence: `git tag -l` is empty, the project version is still
`0.0.1-SNAPSHOT`, and `release.yml` triggers only on `v*`. The changelog's `[Unreleased]` section
has accumulated all of SKL-1 with nothing to release it against.

Doing this once, manually and deliberately, is what proves the pipeline works — and is far cheaper
than discovering it is broken under pressure later.

Steps: settle the version (`0.1.0` — pre-1.0, breaking changes permitted in minors, which is honest
for a skeleton), `mvn versions:set -DnewVersion=0.1.0 -f code/pom.xml`, move `[Unreleased]` to
`## [0.1.0] - <date>`, tag `v0.1.0`, push, verify the workflow attaches the jar.

**Acceptance**
- [ ] `v0.1.0` tagged; `release.yml` ran green and attached `boot-0.1.0.jar`
- [ ] `CHANGELOG.md` has a dated `[0.1.0]` section and a fresh empty `[Unreleased]`
- [ ] Version consistent across POMs, jar, changelog and tag
- [ ] Process documented in [../../build/versioning-and-releases.md](../../build/versioning-and-releases.md)

**Blocked by** SKL-23, SKL-29

---

Related: [../known-gaps.md](../known-gaps.md) entries 8, 14, 15;
[../../build/versioning-and-releases.md](../../build/versioning-and-releases.md)
