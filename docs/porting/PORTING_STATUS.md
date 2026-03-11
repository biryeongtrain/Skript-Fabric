# Skript-Fabric Porting Status

Last condensed: 2026-03-11
Last full verification: 2026-03-11

## Snapshot

- Exact-path snapshot against upstream `e6ec744`:
  - overall missing: `351`
  - expressions missing: `153`
  - events missing: `12`
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
  - local `ch/njol/skript`: `140`
  - shortfall: `1049`
- Latest full verification:
  - `./gradlew isolatedExpressionLaneACompatibilityTest isolatedExpressionLaneABindingTest isolatedExpressionLaneBCompatibilityTest isolatedExpressionLaneBBindingTest isolatedMixedRuntimeSyntaxBatchTest test --tests ch.njol.skript.events.FabricPlayerEventHandlesUnitTest --tests org.skriptlang.skript.fabric.runtime.PlayerEventBindingTest --tests org.skriptlang.skript.fabric.compat.CompatAccessorMigrationUnitTest` passed
  - `./gradlew runGameTest --rerun-tasks` passed with `260 / 260`

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- `SkriptParser.parseStatic(...)` now matches with `ALL_FLAGS`, so legacy `SyntaxElementInfo` paths accept expression-only placeholders again.
- `Classes.getPatternInfos(...)` now matches upstream by considering only explicit literal patterns and preserving registration order.
- `Function.execute(...)` now matches upstream keyed plural default behavior instead of zipping multi-value defaults into keyed pairs.
- Latest landed expression slice:
  - lane A vector/location expressions: `ExprLocationFromVector`, `ExprLocationVectorOffset`, `ExprMidpoint`, `ExprVectorBetweenLocations`, `ExprVectorCrossProduct`, `ExprVectorDotProduct`, `ExprVectorLength`, `ExprVectorNormalize`, `ExprXYZComponent`, `ExprYawPitch`
  - lane B server/session snapshot expressions: `ExprMOTD`, `ExprOnlinePlayersCount`, `ExprOps`, `ExprVersion`, `ExprViewDistance`, `ExprWhitelist`
  - landed with unit JUnit, bootstrap/binding JUnit, and Minecraft GameTest
- Latest landed infra slice:
  - live player/session event backends: `EvtCommand`, `EvtMove`, `EvtPlayerChunkEnter`, `EvtPlayerCommandSend`, `EvtResourcePackResponse`, `EvtTeleport`, `EvtSpectate`, `EvtLevel`, `EvtExperienceChange`
  - compat access migration: `PrivateBlockEntityAccess`, `PrivateFurnaceAccess`, and `PrivateFishingHookAccess` now use mixin accessors or invokers instead of reflection
- New regressions lock:
  - `SkriptParserStaticFlagsCompatibilityTest`
  - `FunctionOverloadDisambiguationImplementationTest`
  - `FunctionDefaultKeyedParameterCompatibilityTest`

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
