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

## Standing rule

Closing any ticket includes updating affected lode files **in the same change**. A ticket whose lode
is stale is not Done. This is the mechanism that keeps this epic from regenerating itself.

---

Related: [../known-gaps.md](../known-gaps.md) entries 9, 10
