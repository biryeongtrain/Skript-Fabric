# Skript-Fabric Porting Status

Last condensed: 2026-03-13
Last full verification: 2026-03-13

## Snapshot

- Exact-path snapshot against upstream `e6ec744`:
  - overall missing: `261`
  - expressions missing: `75`
  - events missing: `0`
  - sections missing: `8`
  - command missing: `9`
  - aliases missing: `9`
  - exact-path missing in `conditions`, `effects`, `lang`, `config`, `patterns`, `registrations`: `0`
- Source ports complete:
  - conditions: `28 / 28`
  - expressions: `84 / 84`
  - effects: `24 / 24`
- Runtime-backed `Evt*.java`: `48 / 53`
- Synthetic/partial `Evt*.java`: `0 / 53`
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
  - exact-path present locally: `928`
  - shortfall: `261`
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.expressions.ExpressionCycle20260313JCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313JBindingCompatibilityTest --warning-mode none --console=plain` passed
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `313 / 313` GameTests green on `main`

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- Latest landed runtime/GameTest slice:
  - `ExprAppliedEffect`, `ExprNearestEntity`, and `ExprTargetedBlock` now exist in the Fabric port with upstream-exact paths
  - runtime bootstrap force-initializes the cycle J expression bundle during full GameTest startup
  - cycle J adds targeted compatibility/binding JUnit plus a dedicated real `.sk` GameTest bundle for beacon effect, nearest-entity lookup, and targeted-block lookup
  - cycle J GameTests use real runtime state: live beacon apply-effects, spawned entity distance resolution, and a real player ray against a looked-at block
- Landed with unit JUnit plus targeted Minecraft GameTest; full suite now completes `313 / 313` GameTests green on `main`

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
