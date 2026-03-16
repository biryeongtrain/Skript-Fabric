# Skript-Fabric Porting Status

Last condensed: 2026-03-16
Last full verification: 2026-03-16

## Snapshot

- Exact-path snapshot against upstream `e6ec744`:
  - overall missing: `174`
  - expressions missing: `2` (package-info.java + ExprPlugins.java — both Non-goal)
  - events missing: `0`
  - sections missing: `0`
  - conditions missing: `0`
  - effects missing: `0`
  - command missing: `6`
  - aliases missing: `9`
  - structures missing: `2` (StructAliases, StructAutoReload — Adapt/Non-goal)
  - literals missing: `1` (LitAt)
  - exact-path missing in `lang`, `config`, `patterns`, `registrations`: `0`
- Source ports complete:
  - conditions: `28 / 28`
  - expressions: `85 / 85`
  - effects: `24 / 24`
  - sections: `9 / 9`
- Runtime-backed `Evt*.java`: `52 / 53`
- Synthetic/partial `Evt*.java`: `0 / 53`
- Non-runtime/manual `Evt*.java`: `1 / 53`
- Total `Evt*.java` files: `86` (includes duplicates across packages and Fabric-specific events)
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete packages:
  - `breeding`: `12 / 12`
  - `input`: `5 / 5`
  - `interactions`: `6 / 6`
- Remaining package-local Stage 8 scope: `191 / 214`
- Top-level non-package Bukkit helpers outside that matrix: `4`
- Upstream core audit baseline:
  - upstream `ch/njol/skript` snapshot `e6ec744`: `1189`
  - exact-path present locally: `1015`
  - shortfall: `174`
- Latest verification:
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `369 / 372` GameTests (3 pre-existing parse-diagnostic test failures)
  - `.sk` script files in `src/gametest/resources`: `410`

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

## Confirmed Behavior Gaps

- The exact-path tracker does not catch registration, class-info, or runtime-surface gaps inside existing files.
- Confirmed gaps as of 2026-03-16:
  - Fully unusable user-visible surfaces:
  - `auto reload` is unavailable.
  - There is no local `StructAutoReload`, and no bootstrap registration path for it.
  - Recently closed gaps:
  - `GameEffectClassInfo` now registered — `gameeffect` type available in typed syntax positions (function parameters, variables).
  - `ParticleClassInfo` was already registered — `particle` type works in typed positions.
  - `rgb(red, green, blue)` function now available in `DefaultFunctions`.
  - `EvtGameMode` pattern aligned with upstream (`%gamemode%` instead of `%-gamemode%`).
  - `EvtWeatherChange` pattern aligned with upstream (`%-weathertypes%` plural form).
  - `EvtResourcePackResponse` split into two patterns matching upstream.
  - Partially unusable or narrowed user-visible surfaces:
  - `EvtClick` drops upstream `/blockdata` targeting (needs `blockdata` type registration).
  - `EvtHarvestBlock` drops upstream `/blockdatas` targeting (needs `blockdatas` type registration).
  - `EvtPlayerArmorChange` uses `armorslot` type instead of upstream `equipmentslot`.

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
  - cycle N ports `SecFilter`, `SecFor`, `ExprTransform`, `ExprValueWithin` (loop/filter/transform expressions)
  - cycle O ports full `org.skriptlang.skript.log.runtime` package (RuntimeError, RuntimeErrorManager, RuntimeErrorCatcher, ErrorSource, Frame, RuntimeErrorFilter, RuntimeErrorConsumer, RuntimeErrorProducer, SyntaxRuntimeErrorProducer) + `SecCatchErrors` + `Skript.getRuntimeErrorManager()`
  - cycle P ports remaining sections: `SecWhile` (while/do-while loops), `ExprSecCreateWorldBorder` (virtual world borders), `EffSecSpawn` (entity spawning with section), `EffSecShoot` (projectile shooting with section) + adds `EntityData.spawn()` Fabric-native entity spawning infrastructure
  - function declaration loading now lands `StructFunction` plus bootstrap registration for `EffReturn`
  - real `.sk` GameTest now proves declared global and local functions execute during runtime load
  - runtime bootstrap force-initializes the landed cycle F expression bundles during full GameTest startup
  - cycle F adds targeted compatibility/binding JUnit plus dedicated real `.sk` GameTests for every surviving worker lane
- Player session events `on join`, `on connect`, `on kick`, `on quit` now landed with full mixin/Fabric API backing, `SkriptTextPlaceholders` Skript expression resolution, and `PlayerClassInfo` Parser for proper player name display
- Cycle 17 — expression parsing and runtime fixes:
  - `ExprLoopValue` regex constrained: `<.+>` → `<[\\w-]+>` to prevent greedy match of arithmetic operators (fixes `loop-iteration-2 / 30` parsing)
  - `ExprArithmetic.error()` error noise suppressed during parsing backtracking
  - `Variable.newInstance()` type-hint mismatch error noise suppressed during parsing backtracking
  - `ExprTimes` ParsingStack guard: rejects init when ExprArithmetic is on the stack (fixes `loop N times` vs arithmetic ambiguity)
  - 2-pass `parseRegisteredExpression`: type-specific candidates first, Object-returning wildcards second (fixes `event-player` being consumed as arithmetic)
  - `DefaultOperations.register()` call added (fixes ExprArithmetic having zero patterns)
  - `DefaultComparators.register()`, `DefaultConverters.register()` calls added
  - `ExprLoopIteration` forceInitialize added (fixes `loop-iteration-2` not resolving)
  - `vector(x,y,z)` function registered in DefaultFunctions (fixes `vector(0, expr, 0)` syntax)
  - `location(x,y,z,[yaw],[pitch])` function registered in DefaultFunctions
  - `clamp(value,min,max)` function registered in DefaultFunctions
  - GameTest additions: `arithmeticDivisionTimesParsesAsExprTimes`, `eventPlayerIsNotParsedAsArithmetic`, `nestedLoopIterationReferencesCorrectLoop`
- Landed with targeted Minecraft GameTest; current full suite completes `369 / 372` GameTests (3 pre-existing parse-diagnostic failures)

## Open Gaps

- Broader parser default-value and pattern-element parity.
- Broader statement/loader orchestration only when a concrete mismatch is reproduced.
- Function namespace/default-parameter/runtime parity beyond the current fixes.
- Variable runtime is still an in-memory bridge, not upstream-complete.
- Cross-cutting Stage 8 parity gap: ambiguous bare item-id compare, for example `event-item is wheat`.
- `StructCommand` (user-defined `/command` blocks) not yet ported — requires Brigadier adapter.
- `entity effect` type (upstream `entityeffect` class-info) not yet registered — Fabric has no direct enum equivalent.
- `blockdata` type not registered — blocks `EvtClick` and `EvtHarvestBlock` upstream-exact patterns.
- `equipmentslot` type not registered — blocks `EvtPlayerArmorChange` upstream-exact pattern.
- `printLog(true)` in Statement/ScriptLoader still outputs error noise from failed sub-attempts on successful parse.

## Reference Docs

- Upstream closure tracker: [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
- Stage tracker: [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
- Event bridge: [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
- Active syntax surface: [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
