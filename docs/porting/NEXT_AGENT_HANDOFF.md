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
- Runtime-backed `Evt*.java`: `40 / 50`
- Synthetic/partial `Evt*.java`: `5 / 50`
- Non-runtime/manual `Evt*.java`: `5 / 50`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Upstream `ch/njol/skript` baseline: exact-path present `925`, upstream `1189`, shortfall `264`
- Latest verification:
  - `./gradlew test --tests org.skriptlang.skript.fabric.runtime.FirstJoinRuntimeUnitTest --tests org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridgeMoveOnTest --tests ch.njol.skript.events.EventCompatibilityTest --warning-mode none --console=plain` passed
  - `build/junit.xml` recorded `first_join_event_executes_real_script`, `walking_on_dirt_event_executes_real_script`, `respawn_event_executes_real_script`, `explosion_prime_producer_executes_real_script`, and `player_unleash_producer_executes_real_script` as passing GameTests
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` passed; `278 / 278`

## Most Recent Merged Slice

- public `on first join:` now uses the real `PlayerList.placeNewPlayer(...)` producer and a dedicated player-infra GameTest
- public `on walking on %itemtypes%:` now uses the accepted player-move path with support-block delta tracking and a dedicated player-infra GameTest
- public `on respawn:` now lives in a dedicated player-infra GameTest resource instead of the shared custom-context backfill
- public `on explosion prime:` producer coverage now lives in the event suite with a dedicated fixture instead of the shared custom-context backfill
- public `on player unleashing:` now carries the real leash-holder actor through `Leashable.dropLeash(...)` and a dedicated public-syntax GameTest
- `EvtLeash` remains partial only for `leash`

## Do Next

- Continue event hook closure from the remaining synthetic/partial bucket:
  - `EvtBlock`
  - `EvtItem`
  - `EvtHarvestBlock`
  - `EvtLeash` (`leash` remaining)
  - `EvtWorld`
- Continue removing synthetic event aliases only where public syntax plus real producer both exist:
  - remaining blocked aliases are mutable `entity death`, `gametest area cloud effect`, `gametest player experience cooldown change`, `gametest hanging break`, and `gametest block fertilize`
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
