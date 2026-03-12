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
- Runtime-backed `Evt*.java`: `40 / 50`
- Synthetic/partial `Evt*.java`: `5 / 50`
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
  - `./gradlew test --tests org.skriptlang.skript.fabric.runtime.FirstJoinRuntimeUnitTest --tests org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridgeMoveOnTest --tests ch.njol.skript.events.EventCompatibilityTest --warning-mode none --console=plain` passed
  - `build/junit.xml` recorded `first_join_event_executes_real_script`, `walking_on_dirt_event_executes_real_script`, `respawn_event_executes_real_script`, `explosion_prime_producer_executes_real_script`, and `player_unleash_producer_executes_real_script` as passing GameTests
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` passed; `278 / 278`

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- Latest landed runtime/GameTest slice:
  - public `on first join:` now uses the real `PlayerList.placeNewPlayer(...)` producer and a dedicated player-infra GameTest
  - public `on walking on %itemtypes%:` now uses the accepted player-move path with support-block delta tracking and a dedicated player-infra GameTest
  - public `on respawn:` now lives in a dedicated player-infra GameTest resource instead of the shared custom-context backfill
  - public `on explosion prime:` producer coverage now lives in the event suite with a dedicated fixture instead of the shared custom-context backfill
  - public `on player unleashing:` now carries the real leash-holder actor through `Leashable.dropLeash(...)` and a dedicated public-syntax GameTest
- Deferred from the same cycle:
  - `EvtLeash` remains partial for `leash` only
  - synthetic backfill still remains for mutable `entity death`, `area cloud effect`, `player experience cooldown change`, `hanging break`, and `block fertilize`
- Landed with unit JUnit plus targeted Minecraft GameTest; full suite now passes `278 / 278`

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
