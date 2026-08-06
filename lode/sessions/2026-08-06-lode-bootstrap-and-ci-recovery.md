# Session: lode bootstrap and CI recovery — 2026-08-06

## Context

The project had no `lode/`; knowledge lived in root markdown (`README.md`, `AGENTS.MD`,
`KNOWLEDGE-BASE.md`, `CONFIGURATIONS.md`) that had drifted from the code. The goal was to build the
lode and a `SKL-` backlog. Auditing code against its own documentation turned out to be the real
deliverable: it exposed eight defects, three of which had CI red and unread.

## Approaches tried

### Deriving current state from the existing root docs
- **Tried**: seeding the lode from `README.md`, `AGENTS.MD` and `KNOWLEDGE-BASE.md`.
- **Outcome**: rejected as a primary source. Twelve files named in `README.md` did not exist;
  `AGENTS.MD`'s dependency graph did not match the POMs; `KNOWLEDGE-BASE.md` asserted
  `application-local.yml` was git-ignored when it was tracked.
- **Failure mode**: the docs described intent, not implementation, with no marker distinguishing
  the two. Anything derived from them inherited the fiction.
- **What worked instead**: reading POMs, sources and configs directly, then treating each root-doc
  claim as a hypothesis to verify.

### Diagnosing the CI boot failure as a service-readiness race
- **Tried**: attributing `Unable to determine Dialect without JDBC metadata` to Postgres not being
  ready — GitHub service containers have no health gate, and `KNOWLEDGE-BASE.md` §17 documents
  exactly that race.
- **Outcome**: wrong. Reproduced locally by exporting the three `SPRING_DATASOURCE_*` variables the
  workflow sets; failed identically with no Postgres involved.
- **Failure mode**: env vars outrank profile YAML, so the Postgres url replaced the H2 url in
  `application-test.yml` while `driver-class-name: org.h2.Driver` stayed — a self-inconsistent
  datasource. The real error line, `Driver org.h2.Driver claims to not accept jdbcUrl`, was several
  frames above the one that looked diagnostic.
- **Lesson**: a plausible documented cause is not evidence. Reproduce before fixing.

### Trusting `mvn verify` exit code as proof the suite runs
- **Tried**: treating BUILD SUCCESS as confirmation the Testcontainers IT passed.
- **Outcome**: rejected. `Tests run: 1` — only the context-load test executed.
- **Failure mode**: `maven-failsafe-plugin` is unconfigured and surefire's default includes do not
  match `*IT`. The suite reports success while skipping. Louder than a compile error precisely
  because it is silent. → `SKL-32`.

### Copying the thesis repo's slash commands verbatim
- **Tried**: dropping `closeout.md` and `reanchor.md` in unchanged.
- **Outcome**: rejected. They referenced `lode/physics/`, paper-section sync, `CHECKPOINT.md` and
  `future-work.md` — none exist here. Adapted to this repo's modules and ticket-based backlog.

## Unresolved questions

- [ ] Does `maven-failsafe-plugin` need Docker on CI runners for `ExamplePostgresAdapterIT`, and
      does the current `ubuntu-latest` image provide it? Blocks verifying `SKL-32` end to end.
- [ ] Avro or JSON for Kafka payloads (`SKL-9`)? Gates `SKL-6`; choosing after the producer exists
      means rewriting it.
- [ ] Should `contract` keep a live `@RestController`, or should the controller be generated from
      `openapi.yml` (`SKL-20`)? The delegate interface already matches the generator's shape.
- [ ] Is the double dependency resolution per CI run (`SKL-36`) worth fixing before pruning the
      307-dependency `infrastructure` module (`SKL-10`), or does pruning make it moot?

## Partial insights

- `infrastructure` resolving **307 dependencies** for five classes is not just conceptual debt — it
  is roughly two minutes of CI wall clock, paid twice per run. Not yet measured against a warm
  cache, so the true marginal cost of `SKL-10` is unconfirmed.
- The `.gitignore` `build/` rule (NetBeans) silently swallowed `lode/build/`. Suspect the same class
  of collision exists for any future lode directory named `bin`, `out` or `target`. Only `build/`
  has been confirmed and negated.
- Spring Boot 4 relocations found so far follow a pattern: `org.springframework.boot.<technology>.
  <concern>.autoconfigure`. Held for metrics, health, JPA test and JDBC test. Not verified broadly
  enough to state as a rule in `maven-conventions.md`.

## Graduation candidates

- The CI-vs-local verification checklist — **graduated** this session to
  `lode/practices.md` §Verification.
- The Boot 4 relocation table — **graduated** to `lode/build/maven-conventions.md` §8–9.
- The dependency-count-per-module measurement — currently only in `SKL-36`. Promote to
  `lode/build/summary.md` once a warm-cache baseline exists to compare against.
