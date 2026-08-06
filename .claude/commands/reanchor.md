# Reanchor Session

Use this when the session has drifted — when responses feel misaligned with the project, when
assumptions start contradicting established conventions, or when context has grown stale after many
exchanges.

## Step 1 — State the current task
In one sentence, state what you are currently trying to accomplish.

## Step 2 — Re-read core lode files
Read unconditionally:
- `lode/lode-map.md`
- `lode/summary.md`
- `lode/practices.md`
- `lode/terminology.md`

## Step 3 — Re-read the backlog
Read `lode/plans/backlog.md`. It carries ticket statuses, sprint ordering, and the dependency graph.
If the current task maps to a ticket, read its detail in `lode/plans/tickets/<epic>.md` — acceptance
criteria and prior decisions are recorded there, including the reasoning on closed tickets.

Also read `lode/plans/known-gaps.md` — it lists what is currently broken or unimplemented, which is
frequently the reason something is not behaving as expected.

## Step 4 — Re-read task-relevant domain lode files
Use `lode/lode-map.md` as the index. Do not skip this — stale context is usually caused by missing
domain knowledge, not missing summary knowledge.

The invariants most often violated when drifting:
- module dependency direction (`lode/architecture/module-topology.md`)
- `domain` imports no framework
- MapStruct lives only in `infrastructure`
- REST endpoints go through the delegate pattern (`lode/contract/rest-delegate-pattern.md`)

## Step 5 — Check for session lode
If `lode/sessions/` has a file matching today's date or the current topic, read it. It may contain
rejected approaches or unresolved questions directly relevant to the current task.

## Step 6 — Reconcile
If anything re-read contradicts an assumption made earlier this session, **state the contradiction
explicitly** and correct course. Do not silently continue on stale assumptions. If the lode itself
contradicts the code, the code wins — say so and fix the lode.

## Step 7 — Confirm alignment
State in 2–3 sentences: what the current task is, what the relevant constraints are (from the lode),
and what the next concrete action is.

---

**When to use**: call `/reanchor` when you notice drift, after `/compact`, when switching to a
different Maven module mid-session, or when a response surprises you in a way that suggests missing
context.

**Verification reflex**: this project has repeatedly looked correct locally and failed in CI. If the
task involves the build, tests, or CI, re-read `lode/build/ci-cd.md` and `lode/testing/summary.md`
before trusting a green local run.
