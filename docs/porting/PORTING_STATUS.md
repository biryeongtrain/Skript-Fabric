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
- Runtime-backed `Evt*.java`: `30 / 45`
- Synthetic/partial `Evt*.java`: `10 / 45`
- Non-runtime/manual `Evt*.java`: `5 / 45`
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
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest` passed
  - `build/junit.xml` recorded `mixed_damage_and_healing_syntax_executes_real_script` and `unleash_producer_executes_real_script` as passing GameTests
  - full `./gradlew runGameTest --rerun-tasks` is currently blocked by the existing `ExprNumbers` GameTest failure; `264 / 265` passed

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- Latest landed runtime/GameTest slice:
  - public `on respawn:` now uses the real `PlayerList.respawn(...)` producer and a dedicated respawn GameTest
  - public `on piglin barter:` now uses the real piglin barter producer and a dedicated barter GameTest
  - public `on player egg throw:` now uses the real `ThrownEgg.onHit(...)` producer and a dedicated egg collision GameTest
  - synthetic mixed-runtime aliases for `gametest respawn`, `gametest piglin barter`, and `gametest player egg throw` were removed in favor of public syntax
- Deferred from the same cycle:
  - `EvtLeash` remains partial for `leash` and `player unleash`
  - synthetic event backfill still required for `explode`, `explosion prime`, and mutable `entity death`
- Landed with unit JUnit plus targeted Minecraft GameTest; full suite is currently blocked only by the unrelated `ExprNumbers` GameTest failure

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
