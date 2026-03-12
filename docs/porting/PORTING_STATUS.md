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
  - exact-path present locally: `925`
  - shortfall: `264`
- Latest full verification:
  - targeted cycle JUnit suite passed for syntax1 G, syntax2 G, syntax3 G, bootstrap/binding G, event compatibility, event bridge binding, and mixed runtime
  - cycle H GameTest hardening passed for syntax1/2/3 real `.sk` coverage and the dedicated `entity shoot bow` trigger path
  - `./gradlew runGameTest --rerun-tasks` passed with `261 / 261`

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- Latest landed expression slice:
  - syntax1 compatibility subset: `ExprQuitReason`, `ExprSourceBlock`, `ExprTamer`
  - syntax2 compatibility subset: `ExprHostname`, `ExprTPS`, `ExprPermissions`
  - syntax3 compatibility subset: `ExprConfig`, `ExprNode`, `ExprScripts`
- Latest landed infra slice:
  - bootstrap and binding coverage for the 2026-03-12g compatibility subset
  - cycle H real `.sk` GameTest hardening for the syntax1/2/3 compatibility subset
  - `entity shoot bow` runtime hook with dedicated real-trigger GameTest; mixed-runtime helper backfill removed
- Deferred from the same cycle:
  - weather-change runtime dispatch stayed out after the mixin target failed full GameTest and was reverted in integrator
  - `ExprNumbers`, `ExprReadiedArrow`, `ExprAppliedEffect`, teleport-cause, spawn-reason, and `PrivateFishingHookAccess.currentState` stayed deferred
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
