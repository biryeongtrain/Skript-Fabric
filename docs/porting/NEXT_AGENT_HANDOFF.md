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
- Runtime-backed `Evt*.java`: `38 / 50`
- Synthetic/partial `Evt*.java`: `7 / 50`
- Non-runtime/manual `Evt*.java`: `5 / 50`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Upstream `ch/njol/skript` baseline: exact-path present `925`, upstream `1189`, shortfall `264`
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.EventBridgeBindingTest --tests org.skriptlang.skript.fabric.runtime.PlayerArmorChangeRuntimeTest` passed
  - `build/junit.xml` recorded `entity_target_and_untarget_execute_real_script`, `entity_portal_executes_real_script`, `helmet_change_executes_real_script`, `explosion_executes_real_script`, `explosion_prime_producer_executes_real_script`, and `mutable_entity_death_payload_backfill_executes_synthetic_script` as passing GameTests
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` passed; `276 / 276`

## Most Recent Merged Slice

- public `on [entity] target:` / `on [entity] un-target:` now uses the real `Mob.setTarget(...)` producer and a dedicated GameTest
- public `on [player] portal:` / `on entity portal:` now uses the real portal travel path and a dedicated GameTest
- public `on armor change:` / `on helmet change:` now uses the real `LivingEntity.onEquipItem(...)` producer and a dedicated GameTest
- public `on explode:` now uses the real TNT producer with mutable block-yield feedback and dedicated GameTests
- public `on explosion prime:` now uses the real creeper ignition path with mutable radius feedback and dedicated GameTests
- mixed-runtime synthetic aliases for `gametest explode` and `gametest explosion prime` were removed
- mutable `entity death` payload proof was moved out of the shared bundle into a dedicated backfill GameTest
- `EvtLeash` remains partial: `unleash` is live, `leash` and `player unleash` still need dedicated runtime coverage

## Do Next

- Continue event hook closure from the remaining synthetic/partial bucket:
  - `EvtBlock`
  - `EvtItem`
  - `EvtFirstJoin`
  - `EvtHarvestBlock`
  - `EvtLeash` (`leash` and `player unleash` remaining)
  - `EvtMoveOn`
  - `EvtWorld`
- Continue removing synthetic event aliases only where public syntax plus real producer both exist:
  - remaining blocked alias is mutable `entity death`
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
