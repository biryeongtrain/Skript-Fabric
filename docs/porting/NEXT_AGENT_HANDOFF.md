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
- Runtime-backed `Evt*.java`: `30 / 45`
- Synthetic/partial `Evt*.java`: `10 / 45`
- Non-runtime/manual `Evt*.java`: `5 / 45`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Upstream `ch/njol/skript` baseline: exact-path present `925`, upstream `1189`, shortfall `264`
- Latest full verification:
  - targeted cycle JUnit suite passed for event compatibility, event cycle compatibility, event bridge binding, and mixed runtime
  - `./gradlew runGameTest --rerun-tasks` passed with `264 / 264`

## Most Recent Merged Slice

- `EvtEntityBlockChange` live sheep-eat path
- `EvtGameMode`
- `EvtWeatherChange`
- `EvtPressurePlate`
- `EvtVehicleCollision` minecart/entity path
- `EvtFirework`
- real `unleash` runtime backfill replaced the synthetic mixed-runtime helper path
- minimal bootstrap registration for `EvtWeatherChange`
- firework GameTest runtime-lock stabilization for full-suite execution

## Do Next

- Continue event hook closure from the remaining synthetic/partial bucket:
  - `EvtBlock`
  - `EvtItem`
  - `EvtEntityTarget`
  - `EvtFirstJoin`
  - `EvtHarvestBlock`
  - `EvtLeash`
  - `EvtMoveOn`
  - `EvtPlayerArmorChange`
  - `EvtPortal`
  - `EvtWorld`
- After that, resume exact-path closure from `264` overall missing with focus on expressions `78` and the remaining non-event buckets.
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
