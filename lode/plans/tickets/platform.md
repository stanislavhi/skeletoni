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
