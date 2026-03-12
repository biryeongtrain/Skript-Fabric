# Next Agent Handoff

Last condensed: 2026-03-13
Last full verification: 2026-03-13

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
- Runtime-backed `Evt*.java`: `48 / 53`
- Synthetic/partial `Evt*.java`: `0 / 53`
- Non-runtime/manual `Evt*.java`: `5 / 53`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Upstream `ch/njol/skript` baseline: exact-path present `925`, upstream `1189`, shortfall `264`
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.HarvestBlockRuntimeTest --warning-mode none --console=plain` passed
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `309` GameTests; `block place`, `block mine`, `inventory click`, and `sweet berry harvest` passed, and the remaining failure was `skript_fabric_expression_cycle_isyntax1game_test_expr_numbers_executes_real_script`

## Most Recent Merged Slice

- public `on area cloud effect:`, `on player experience cooldown change:`, and `on block fertilize:` now use public syntax with real-trigger GameTests
- `EvtBlock` now has live `burn`, `fade`, `form`, `drop`, `break`, `mine`, and `place` producers
- `EvtItem` now has live `dispense`, `player/entity drop`, `prepare craft`, `craft`, `player/entity pickup`, `consume`, `item despawn`, `item merge`, `inventory item move`, `inventory click`, and `stonecutting` producers
- `EvtHarvestBlock` now uses the real ripe `SweetBerryBushBlock.useWithoutItem(...)` harvest path
- public `on player leashing:` now uses the real `Leashable.setLeashedTo(...)` attach path
- `EvtWorld` now has live `save`, `initialization`, `loading`, and `unloading` producers
- runtime bootstrap now force-initializes the recovered event activation bundle during full GameTest startup

## Do Next

- Event-hook closure for runtime-backed `Evt*.java` is complete; keep docs and tests aligned with `48 / 53` live and `5 / 53` non-runtime/manual.
- Event-facing synthetic alias cleanup is also closed for the remaining hanging payload case; do not reintroduce `gametest ...` event aliases where public syntax plus real producer already exist.
- Investigate the remaining unrelated full-suite failure:
  - `skript_fabric_event_game_test_primary_beacon_effect_executes_real_script`
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
