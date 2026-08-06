# Session Closeout

Perform a complete session closeout. Work through every step in order without skipping.

## Step 1 — Identify what changed
Run `git status` and `git diff HEAD` to get a precise list of changed files and modules.
Note which Maven modules under `code/` were touched — that drives Step 2.

## Step 2 — Update ALL affected lode topic files
For every area touched this session, read the corresponding lode file and rewrite it to reflect
**current system state**. No historical commentary ("we added X today"). Every sentence must
describe how the system works now.

Map from what changed to what to update:

| Changed | Lode to update |
|---|---|
| `code/contract/` | `lode/contract/` |
| `code/domain/` | `lode/domain/summary.md` |
| `code/application/` | `lode/application/summary.md` |
| `code/infrastructure/` | `lode/infrastructure/` |
| `code/logging/`, `code/observability/` | `lode/observability/` |
| `code/resilience/` | `lode/resilience/summary.md` |
| any `pom.xml`, `Dockerfile` | `lode/build/` |
| `.github/workflows/` | `lode/build/ci-cd.md` |
| `application*.yml`, `compose.yml`, `infra/` | `lode/local-dev/` |
| any test, surefire/failsafe config | `lode/testing/summary.md` |
| module dependency edges | `lode/architecture/module-topology.md` |
| request/response path | `lode/architecture/request-flow.md` |

## Step 3 — Update top-level lode files
- `lode/summary.md` — the one-paragraph snapshot and the state-at-a-glance table
- `lode/terminology.md` — any new terms introduced
- `lode/practices.md` — any new convention or invariant established
- `lode/lode-map.md` — if any lode file was created, renamed, or removed

## Step 4 — Update the backlog
- `lode/plans/backlog.md` — move ticket statuses, add tickets for anything discovered
- `lode/plans/tickets/<epic>.md` — tick acceptance criteria; record the resolution on closed
  tickets so the *reasoning* survives, not just the outcome
- `lode/plans/known-gaps.md` — **delete** entries whose tickets closed. This file is present state,
  not history. Add entries for newly found divergences.

A ticket is not Done until the lode files it touches are updated in the same change.

## Step 5 — Audit for stale lode claims
Scan lode files referencing anything changed this session. Any claim that no longer matches the
code is a defect — fix it, and say so explicitly rather than quietly editing.

## Step 6 — Verify the build, then verify CI
Run `mvn -B clean verify -f code/pom.xml`. Do not claim a change works on a green local build alone.

Before committing, ask what CI supplies or withholds that a developer machine does not:
- Does the change depend on a directory git cannot track? (empty dirs are invisible — `SKL-33`)
- Does CI export env vars that outrank profile YAML? (`SKL-35`)
- Does a tool need a config file that only fails in CI? (`SKL-34`)
- Does the test actually execute, or is it silently skipped? (`*IT` needs failsafe — `SKL-32`)

Check the test count, not just the exit code.

## Step 7 — Update the changelog
Add entries to `[Unreleased]` in `CHANGELOG.md`, keyed by ticket ID, grouped under Keep a Changelog
headings (`Added`, `Changed`, `Removed`, `Fixed`). Every behaviour change gets one. Format and
release process: `lode/build/versioning-and-releases.md`.

## Step 8 — Write session lode (if warranted)
If the session produced rejected approaches or unresolved questions worth keeping, create
`lode/sessions/YYYY-MM-DD-<topic>.md` per `~/IdeaProjects/engineering-lode/session-lode-template.md`:
Context, Approaches tried (with specific failure modes), Unresolved questions, Partial insights,
Graduation candidates.

A knowledge staging area — not a diary, not a changelog. Skip it if the session produced only
validated knowledge; that belongs in permanent lode files.

## Step 9 — Write handover (if resumable work is in flight)
`lode/tmp/handover-YYYY-MM-DD.md`: current task state, decisions made, approaches tried, blockers,
exact next steps. `lode/tmp/` is git-ignored.

## Step 10 — Clean `lode/tmp/`
Delete handovers from previous sessions that no longer apply.

## Step 11 — Update root docs if conventions changed
This repo has no `CLAUDE.md`. Agent-facing conventions live in `AGENTS.MD` — update its ownership
map, module tree, and dependency direction if any changed. Note that `AGENTS.MD`,
`KNOWLEDGE-BASE.md` and `CONFIGURATIONS.md` overlap heavily with the lode and drift easily
(`SKL-26`, `SKL-27`).

## Step 12 — Commit
Stage code + lode + changelog together. One commit per ticket where the files allow it; combine
only when hunks genuinely overlap. Message describes what changed and why, not a file list.
Prefix with the actor per `AGENTS.MD`: `[claude] SKL-<n>: <subject>`. No co-authored-by lines.

## Step 13 — Push, PR, and confirm CI
`gh pr create --base develop`. Then **watch the run to completion** — do not report a PR as done
while CI is pending. If it fails, read the actual failure before theorising; the obvious diagnosis
has been wrong here before.

---

**Lode quality rule**: every lode file must read as a description of the system as it exists right
now. If removing a sentence would not confuse a fresh session, remove it.

**Honesty rule**: if the code contradicts the lode, the code wins — fix the lode and state the
correction out loud. If something is unverified, label it unverified.
