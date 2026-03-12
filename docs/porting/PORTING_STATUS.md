# Skript-Fabric Porting Status

Last condensed: 2026-03-12
Last full verification: 2026-03-12

## Snapshot

- Exact-path snapshot against upstream `e6ec744`:
  - overall missing: `264`
  - expressions missing: `78`
  - events missing: `0`
  - sections missing: `8`
  - command missing: `9`
  - aliases missing: `9`
  - exact-path missing in `conditions`, `effects`, `lang`, `config`, `patterns`, `registrations`: `0`
- Source ports complete:
  - conditions: `28 / 28`
  - expressions: `84 / 84`
  - effects: `24 / 24`
- Runtime-backed `Evt*.java`: `38 / 50`
- Synthetic/partial `Evt*.java`: `7 / 50`
- Non-runtime/manual `Evt*.java`: `5 / 50`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete packages:
  - `breeding`: `12 / 12`
  - `input`: `5 / 5`
  - `interactions`: `6 / 6`
- Remaining package-local Stage 8 scope: `191 / 214`
- Top-level non-package Bukkit helpers outside that matrix: `4`
- Upstream core audit baseline:
  - upstream `ch/njol/skript` snapshot `e6ec744`: `1189`
  - exact-path present locally: `925`
  - shortfall: `264`
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.EventBridgeBindingTest --tests org.skriptlang.skript.fabric.runtime.PlayerArmorChangeRuntimeTest` passed
  - `build/junit.xml` recorded `entity_target_and_untarget_execute_real_script`, `entity_portal_executes_real_script`, `helmet_change_executes_real_script`, `explosion_executes_real_script`, `explosion_prime_producer_executes_real_script`, and `mutable_entity_death_payload_backfill_executes_synthetic_script` as passing GameTests
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` passed; `276 / 276`

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- Latest landed runtime/GameTest slice:
  - public `on [entity] target:` / `on [entity] un-target:` now uses the real `Mob.setTarget(...)` producer and a dedicated entity target GameTest
  - public `on [player] portal:` / `on entity portal:` now uses the real portal travel path and a dedicated entity portal GameTest
  - public `on armor change:` / `on helmet change:` now uses the real `LivingEntity.onEquipItem(...)` producer and a dedicated armor-slot GameTest
  - public `on explode:` now uses the real `ServerExplosion.explode()` producer with mutable block-yield feedback and dedicated TNT-triggered GameTests
  - public `on explosion prime:` now uses the real creeper ignition path with mutable radius feedback and dedicated real-trigger GameTests
  - mixed-runtime synthetic aliases for `gametest explode` and `gametest explosion prime` were removed in favor of public syntax
  - mutable `entity death` payload proof was split out of the shared mixed bundle into a dedicated backfill GameTest resource
- Deferred from the same cycle:
  - `EvtLeash` remains partial for `leash` and `player unleash`
  - synthetic backfill still remains for mutable `entity death` only
- Landed with unit JUnit plus targeted Minecraft GameTest; full suite now passes `276 / 276`

## Open Gaps

- Broader parser default-value and pattern-element parity.
- Broader statement/loader orchestration only when a concrete mismatch is reproduced.
- Function namespace/default-parameter/runtime parity beyond the current fixes.
- Variable runtime is still an in-memory bridge, not upstream-complete.
- Cross-cutting Stage 8 parity gap: ambiguous bare item-id compare, for example `event-item is wheat`.

## Reference Docs

- Upstream closure tracker: [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
- Stage tracker: [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
- Event bridge: [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
- Active syntax surface: [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
