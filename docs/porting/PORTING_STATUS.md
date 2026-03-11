# Skript-Fabric Porting Status

Last condensed: 2026-03-11
Last full verification: 2026-03-11

## Snapshot

- Exact-path snapshot against upstream `e6ec744`:
  - overall missing: `314`
  - expressions missing: `123`
  - events missing: `5`
  - sections missing: `8`
  - command missing: `9`
  - aliases missing: `9`
  - exact-path missing in `conditions`, `effects`, `lang`, `config`, `patterns`, `registrations`: `0`
- Source ports complete:
  - conditions: `28 / 28`
  - expressions: `84 / 84`
  - effects: `24 / 24`
- Stage 5 event backend rows active: `22 / 22`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete packages:
  - `breeding`: `12 / 12`
  - `input`: `5 / 5`
  - `interactions`: `6 / 6`
- Remaining package-local Stage 8 scope: `191 / 214`
- Top-level non-package Bukkit helpers outside that matrix: `4`
- Upstream core audit baseline:
  - upstream `ch/njol/skript` snapshot `e6ec744`: `1189`
  - exact-path present locally: `875`
  - shortfall: `314`
- Latest full verification:
  - targeted cycle JUnit suite passed for vector geometry, syntax S2/S3/S4, mixed runtime binding, event compatibility, scheduled/experience/script lifecycle runtime, and compat accessor migration
  - `./gradlew runGameTest --rerun-tasks` passed with `260 / 260`

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- Latest landed expression slice:
  - vector geometry: `ExprVectorAngleBetween`, `ExprVectorFromXYZ`, `ExprVectorOfLocation`, `ExprVectorProjection`, `ExprVectorRandom`, `ExprVectorSquaredLength`
  - world/time and border: `ExprTemperature`, `ExprTime`, `ExprWorld`, `ExprWorldEnvironment`, `ExprWorldFromName`, `ExprWorlds`, `ExprWorldBorder`, `ExprWorldBorderCenter`, `ExprWorldBorderSize`, `ExprWorldBorderDamageAmount`, `ExprWorldBorderDamageBuffer`, `ExprWorldBorderWarningDistance`
  - syntax3 property subset: `ExprUUID`, `ExprVelocity`, `ExprTimeLived`, `ExprScoreboardTags`, `ExprGameMode`, `ExprSaturation`
  - syntax4 relation subset: `ExprPassenger`, `ExprVehicle`, `ExprShooter`, `ExprTarget`, `ExprTransformReason`, `ExprUnleashReason`
- Latest landed infra slice:
  - scheduled and lifecycle event backends: `EvtPeriodical`, `EvtAtTime`, `ExperienceSpawnEvent`, `ScriptEvent`, `SkriptStartEvent`, `SkriptStopEvent`
  - compat access migration: `PrivateBeaconAccess` and `PrivateBellAccess`
- Deferred from the same cycle:
  - `PrivateFishingHookAccess.currentState` migration stayed out of `main` after a GameTest mixin accessor failure
- Landed with unit JUnit, bootstrap/binding JUnit, and Minecraft GameTest

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
