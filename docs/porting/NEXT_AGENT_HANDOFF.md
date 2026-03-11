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
- Upstream `ch/njol/skript` baseline: exact-path present `903`, upstream `1189`, shortfall `286`
- Latest full verification:
  - targeted cycle JUnit suite passed for syntax S1/S2/S3/S4, event compatibility/runtime, mixed runtime binding, and compat accessor migration
  - `./gradlew runGameTest --rerun-tasks` passed with `260 / 260`

## Most Recent Merged Slice

- syntax1 world helpers `ExprGameRule`, `ExprWorldBorderWarningTime`, `ExprWeather`
- syntax2 vector and text helpers plus syntax3 villager/item subset and syntax4 identity/relation subset
- exact-path event closure for `EvtRealTime`, `SimpleEvents`, `ExperienceSpawnEvent`, and `PreScriptLoadEvent`
- allay and item-entity compat accessors
- `PrivateFishingHookAccess.currentState` migration deferred again after the GameTest mixin accessor descriptor failure

## Do Next

- Continue exact-path closure from `286` overall missing with focus on expressions `100` and the remaining non-event buckets.
- Keep `PrivateFishingHookAccess.currentState` out until the accessor target is corrected and revalidated in GameTest.
- Keep Stage 8 package counts unchanged unless you actually audit another package.
- If user-visible `.sk` behavior changes, add real `.sk` coverage and rerun GameTests.

## Guardrails

- Do not claim parity complete.
- Do not re-expand these docs with long logs.
- Do not revive the 5-lane parallel doc expansion.
- Preserve exact counts and exact verification outcomes.
