# Skript-Fabric Porting Status

Last condensed: 2026-03-13
Last full verification: 2026-03-13

## Snapshot

- Exact-path snapshot against upstream `e6ec744`:
  - overall missing: `228`
  - expressions missing: `44`
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
  - exact-path present locally: `961`
  - shortfall: `228`
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.expressions.ExpressionCycle20260313FBindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe1CompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe1BindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe2CompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe2BindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe4CompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe4BindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe5CompatibilityTest --tests org.skriptlang.skript.fabric.runtime.ExpressionCycle20260313FSafe5BindingTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe6CompatibilityTest --warning-mode none --console=plain` passed
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `335 / 335` GameTests green on `main`

## Active Priority

1. Keep the exact-path tracker frozen and accurate, but treat it as bookkeeping rather than a literal parity target.
2. Spend the next cycles on the `Must Port` bucket first: remaining user-visible expressions, remaining sections, remaining literals, and the core runtime pieces they directly depend on.
3. Treat Bukkit-shaped command/alias/storage/chat/server-icon surfaces as `Adapt`, not strict exact-path targets; land Fabric-native equivalents when user-visible parity needs them.
4. Treat plugin-ecosystem, Bukkit utility, hook integration, test harness, doc generator, updater, and bridge/tooling leftovers as `Non-goal` unless a concrete Fabric need appears.

## Reclassification

- `Must Port`
  - Remaining user-visible Skript syntax that still matters on Fabric.
  - Current examples: arithmetic support, remaining entity/block/banner/sign/property expressions, remaining sections, and literals.
- `Adapt`
  - Surfaces that are still user-visible, but where Bukkit and Fabric concepts diverge enough that a literal class-for-class copy is the wrong target.
  - Current examples: `aliases`, `command`, SQL/storage backends, serializer glue, slot/util wrappers, and expression families around chat/playerlist/server icon/plugin state/command metadata/teleport cause/spawn reason.
- `Non-goal`
  - Remaining upstream files that are not worth reproducing on Fabric unless they become direct blockers.
  - Current examples: `bukkitutil`, `hooks`, `test`, `doc`, `timings`, `update`, `ModernSkriptBridge`, `PatcherTool`, `ServerPlatform`, `SkriptUpdater`, `StructAutoReload`, and explicit exclusions such as `ExprPlugins`.

## Latest Closed Core Slice

- Latest landed runtime/GameTest slice:
  - cycle F lands worker-first proven subsets:
    - safe1: `ExprArgument`, `ExprParse`, `ExprParseError`, `ExprValue`
    - safe2: `ExprCommandInfo`, `ExprResult`, `ExprScript`, `ExprScriptsOld`
    - safe4: `ExprHexCode`, `ExprColorFromHexCode`, `ExprRecursiveSize`, `ExprBlockSphere`
    - safe5: `ExprMe`, `ExprTypeOf`, `ExprSkullOwner`, `ExprEnchantmentLevel`, `ExprEnchantments`
    - safe6: `ExprMaxMinecartSpeed`, `ExprMinecartDerailedFlyingVelocity`, `ExprCompassTarget`, `ExprPortal`, `LitConsole`
  - cycle F explicitly drops unproven worker items `ExprCmdCooldownInfo`, `ExprEntities`, `ExprValueWithin`, and the entire safe3 section/literal lane
  - cycle K adds upstream-exact `ExprElement`, `ExprLoopValue`, `ExprLowestHighestSolidBlock`, `ExprResonatingTime`, `ExprRingingTime`, and `ExprXOf`
  - cycle L adds upstream-exact `ExprProjectileForce` and extends the live bow producer with projectile force payload
  - cycle M adds `ExprSkull`, `ExprSignText`, and `ExprSpawnerType`
  - function declaration loading now lands `StructFunction` plus bootstrap registration for `EffReturn`
  - real `.sk` GameTest now proves declared global and local functions execute during runtime load
  - runtime bootstrap force-initializes the landed cycle F expression bundles during full GameTest startup
  - cycle F adds targeted compatibility/binding JUnit plus dedicated real `.sk` GameTests for every surviving worker lane
- Landed with targeted Minecraft GameTest; current full suite completes `335 / 335` GameTests green

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
