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
- Runtime-backed `Evt*.java`: `45 / 53`
- Synthetic/partial `Evt*.java`: `3 / 53`
- Non-runtime/manual `Evt*.java`: `5 / 53`
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
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.EventBridgeBindingTest --tests org.skriptlang.skript.fabric.runtime.WorldLifecycleRuntimeTest --tests org.skriptlang.skript.fabric.runtime.ItemLifecycleRuntimeTest --tests org.skriptlang.skript.fabric.runtime.InventoryMoveRuntimeTest --warning-mode none --console=plain` passed
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `306` GameTests with only the known baseline failure `skript_fabric_expression_cycle_isyntax1game_test_expr_numbers_executes_real_script`

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- Latest landed runtime/GameTest slice:
  - public `on area cloud effect:`, `on player experience cooldown change:`, and `on block fertilize:` now use public syntax with dedicated real-trigger GameTests instead of `gametest ...` aliases
  - `EvtBlock` now has live producers for `burn`, `fade`, `form`, and `drop`
  - `EvtItem` now has live producers for `dispense`, `player/entity drop`, `prepare craft`, `craft`, `player/entity pickup`, `consume`, `item despawn`, `item merge`, `inventory item move`, and `stonecutting`
  - public `on player leashing:` now uses the real `Leashable.setLeashedTo(...)` attach path, and `EvtWorld` now has live `save`, `initialization`, `loading`, and `unloading` producers
  - runtime bootstrap now force-initializes the recovered event activation bundle so full GameTest startup sees the recovered public event syntaxes
  - mixed event backfill coverage was reduced so live block/item families no longer rely on helper-only aliases
- Deferred from the same cycle:
  - partial event families are now limited to `EvtBlock`, `EvtItem`, and `EvtHarvestBlock`
  - `EvtItem` still needs a live `inventory click` producer before the family is fully runtime-backed
  - sweet berry harvest was attempted for `EvtHarvestBlock` but not landed because it introduced new full-suite GameTest regressions
  - the remaining event-facing synthetic alias is `gametest hanging break`
- Landed with unit JUnit plus targeted Minecraft GameTest; full suite now completes `306` GameTests with only the unrelated `ExprNumbers` baseline failure

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
