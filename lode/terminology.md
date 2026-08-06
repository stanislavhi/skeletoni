# Terminology

Domain and project language used across skeletoni. Keep entries as `term — meaning`.

## Project identity

- **skeletoni** — the project itself; a reusable Spring Boot 4 microservice skeleton, not a product.
- **turbo.diesel** — Maven `groupId` and Java package root prefix (`turbo.diesel.skeletoni`).
- **boot module** — the composition root; the only module producing an executable jar (`boot-0.0.1-SNAPSHOT.jar`).
- **code/** — parent container directory holding every functional Maven module; the real reactor root is `code/pom.xml`, not the repo-root `pom.xml`.

## Domain

- **Example** — the single demonstration aggregate. Immutable, Lombok `@Builder`, created via `Example.create(name)`.
- **ExampleId** — value object wrapping a `UUID`; a Java `record` with `generate()` / `fromString()` factories.
- **ExampleCreatedEvent** — domain event record (`id`, `name`, `occurredAt`). Declared and mirrored in AsyncAPI, but **not yet published anywhere**.

## Hexagonal vocabulary

- **Port (in)** — inbound use-case interface owned by `application`, e.g. `ExampleUseCase`. Named `{Entity}UseCase`.
- **Port (out)** — outbound dependency interface owned by `application`, e.g. `ExampleRepository`. Named `{Entity}Repository`, `{Entity}EventPublisher`.
- **Adapter** — infrastructure implementation of an out-port, e.g. `ExamplePostgresAdapter`. Named `{Entity}{Technology}Adapter`.
- **Delegate** — contract-module interface (`ExampleControllerDelegate`) implemented in `application` so the REST controller never touches domain types. See [contract/rest-delegate-pattern.md](contract/rest-delegate-pattern.md).
- **Composition root** — `boot`; wires modules together and owns runtime configuration.

## Naming conventions (enforced by AGENTS.MD)

| Kind | Pattern | Example |
|---|---|---|
| Command | `{Verb}{Entity}Command` | `CreateExampleCommand` |
| Query | `{Verb}{Entity}Query` | `ListExamplesQuery` |
| Domain event | `{Entity}{PastTense}Event` | `ExampleCreatedEvent` |
| Aggregate | `{Entity}` (no suffix) | `Example` |
| Value object | `{Entity}{Attribute}` | `ExampleId` |
| JPA entity | `{Entity}JpaEntity` | `ExampleJpaEntity` |
| Spring Data repo | `{Entity}JpaRepository` | `ExampleJpaRepository` |
| Mongo document | `{Entity}Document` | `ExampleDocument` |
| MapStruct mapper | `{Entity}Mapper` | `ExampleMapper` |

## Backlog

- **`SKL-`** — ticket prefix for all tracked work, matching the branch convention
  (`feature/SKL-<n>`). `SKL-1` is the original scaffolding; numbering is never reused.
- **Epic** — grouping of related tickets, one file under `lode/plans/tickets/`:
  `SKL-CORE`, `SKL-MSG`, `SKL-OBS`, `SKL-QLTY`, `SKL-PLAT`, `SKL-DOC`.
- **Board** — `lode/plans/backlog.md`; ticket statuses, sprint ordering, dependency graph.
- **Known gap** — a present divergence between documented and actual behaviour, in
  `lode/plans/known-gaps.md`. Deleted when its ticket closes; the file is state, not history.
- **Session lode** — `lode/sessions/YYYY-MM-DD-<topic>.md`; staging area for rejected approaches and
  unresolved questions until they graduate to a permanent lode file.
- **Handover** — `lode/tmp/handover-*.md`; consumed once by the next session, then deleted.

## Operational

- **Local profile** — `local`, the default active profile; reads `application-local.yml`.
- **Test profile** — `test`; H2 in-memory, broker/NoSQL auto-configuration excluded.
- **Tasks file** — `{MODEL-NAME}-TASKS.md` at repo root; per-agent audit log mandated by `AGENTS.MD`.
- **Ownership map** — table in `AGENTS.MD` assigning files to agents; currently stale (see [plans/known-gaps.md](plans/known-gaps.md)).
