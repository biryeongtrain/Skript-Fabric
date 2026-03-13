# Skript-Fabric Porting Status

Last condensed: 2026-03-13
Last full verification: 2026-03-13

## Snapshot

- Exact-path snapshot against upstream `e6ec744`:
  - overall missing: `254`
  - expressions missing: `68`
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
  - exact-path present locally: `935`
  - shortfall: `254`
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.expressions.ExpressionCycle20260313KCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313KBindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313LCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313LBindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionSyntaxS4CompatibilityTest --warning-mode none --console=plain` passed
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `318 / 318` GameTests green on `main`

## Active Priority

1. Keep the exact-path tracker frozen and accurate, but treat it as bookkeeping rather than a literal parity target.
2. Spend the next cycles on the `Must Port` bucket first: remaining user-visible expressions, remaining sections, remaining literals, and the core runtime pieces they directly depend on.
3. Treat Bukkit-shaped command/alias/storage/chat/server-icon surfaces as `Adapt`, not strict exact-path targets; land Fabric-native equivalents when user-visible parity needs them.
4. Treat plugin-ecosystem, Bukkit utility, hook integration, test harness, doc generator, updater, and bridge/tooling leftovers as `Non-goal` unless a concrete Fabric need appears.

## Reclassification

- `Must Port`
  - Remaining user-visible Skript syntax that still matters on Fabric.
  - Current examples: parser/runtime-heavy expressions such as `ExprArgument`, `ExprParse`, `ExprParseError`, `ExprValue`, `ExprValueWithin`, arithmetic support, remaining entity/block/banner/enchantment/sign/property expressions, remaining sections, literals, and `StructFunction`.
- `Adapt`
  - Surfaces that are still user-visible, but where Bukkit and Fabric concepts diverge enough that a literal class-for-class copy is the wrong target.
  - Current examples: `aliases`, `command`, SQL/storage backends, serializer glue, slot/util wrappers, and expression families around chat/playerlist/server icon/plugin state/command metadata/teleport cause/spawn reason.
- `Non-goal`
  - Remaining upstream files that are not worth reproducing on Fabric unless they become direct blockers.
  - Current examples: `bukkitutil`, `hooks`, `test`, `doc`, `timings`, `update`, `ModernSkriptBridge`, `PatcherTool`, `ServerPlatform`, `SkriptUpdater`, `StructAutoReload`, and explicit exclusions such as `ExprPlugins`.

## Latest Closed Core Slice

- Latest landed runtime/GameTest slice:
  - cycle K adds upstream-exact `ExprElement`, `ExprLoopValue`, `ExprLowestHighestSolidBlock`, `ExprResonatingTime`, `ExprRingingTime`, and `ExprXOf`
  - cycle L adds upstream-exact `ExprProjectileForce` and extends the live bow producer with projectile force payload
  - runtime bootstrap force-initializes the cycle K/L expression bundle during full GameTest startup
  - cycle K/L add targeted compatibility/binding JUnit plus dedicated real `.sk` GameTests for core element/loop semantics, bell timing, lowest/highest solid block lookup, scaled item/entity literals, and projectile-force lookup
- Landed with unit JUnit plus targeted Minecraft GameTest; full suite now completes `318 / 318` GameTests green on `main`

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
