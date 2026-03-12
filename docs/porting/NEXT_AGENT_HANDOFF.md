# Next Agent Handoff

Last condensed: 2026-03-12
Last full verification: 2026-03-12

## Read Order

1. [README.md](README.md)
2. this file
3. [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
4. [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
5. [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
6. [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
7. [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md) and [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md) only for parallel runs

## Current State

- Source ports complete: conditions `28 / 28`, expressions `84 / 84`, effects `24 / 24`
- Stage 5 event backend rows active: `22 / 22`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Upstream `ch/njol/skript` baseline: exact-path present `925`, upstream `1189`, shortfall `264`
- Latest full verification:
  - targeted cycle JUnit suite passed for syntax1 G, syntax2 G, syntax3 G, bootstrap/binding G, event compatibility, event bridge binding, and mixed runtime
  - cycle H GameTest hardening passed for syntax1/2/3 real `.sk` coverage and the dedicated `entity shoot bow` trigger path
  - `./gradlew runGameTest --rerun-tasks` passed with `261 / 261`

## Most Recent Merged Slice

- syntax1 compatibility helpers `ExprQuitReason`, `ExprSourceBlock`, `ExprTamer`
- syntax2 compatibility helpers `ExprHostname`, `ExprTPS`, `ExprPermissions`
- syntax3 compatibility helpers `ExprConfig`, `ExprNode`, `ExprScripts`
- bootstrap/binding closure for the 2026-03-12g syntax subset
- cycle H real `.sk` GameTest hardening for the syntax1/2/3 subset
- `entity shoot bow` runtime hook with dedicated real-trigger GameTest; mixed-runtime helper backfill removed
- weather-change runtime dispatch was rejected after the mixin target failed full GameTest

## Do Next

- Continue exact-path closure from `264` overall missing with focus on expressions `78` and the remaining non-event buckets.
- Resume the next-smallest slices in this order: `ExprNumbers`, `ExprReadiedArrow`, `ExprAppliedEffect`, teleport cause, spawn reason.
- Keep `PrivateFishingHookAccess.currentState` out until the accessor target is corrected and revalidated in GameTest.
- Keep Stage 8 package counts unchanged unless you actually audit another package.
- If user-visible `.sk` behavior changes, add real `.sk` coverage and rerun GameTests.

## Guardrails

- Do not claim parity complete.
- Do not re-expand these docs with long logs.
- Do not revive the 5-lane parallel doc expansion.
- Do not land syntax without a real `.sk` GameTest that proves parse and runtime behavior.
- Do not land event hooks whose only proof is direct compat-handle dispatch; use a real trigger and remove the replaced helper case from mixed backfill.
- Preserve exact counts and exact verification outcomes.
