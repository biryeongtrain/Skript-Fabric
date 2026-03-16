# `ch/njol/skript` Audit And Closure Plan

Last condensed: 2026-03-16
Baseline snapshot date: 2026-03-08

## Baseline

- Upstream snapshot: `e6ec744`
- Upstream `ch/njol/skript`: `1189` Java files
- Exact-path present locally: `1015`
- Exact-path missing in local tree: `174`
- Exact-path expressions missing: `2` (package-info.java + ExprPlugins.java — both Non-goal)
- Exact-path events / sections / conditions / effects missing: `0 / 0 / 0 / 0`
- Exact-path command / aliases / structures / literals missing: `6 / 9 / 2 / 1`

## Priority Matrix

| Tier | Packages | Why |
| --- | --- | --- |
| `P0` | `lang (81 / 85)` | closest numerically to upstream, still behavior-incomplete |
| `P1` | `classes (5 / 28)`, `config (6 / 20)`, `log (9 / 17)`, `patterns (13 / 14)`, `registrations (3 / 10)`, `sections (1 / 10)`, `structures (2 / 10)`, `util (8 / 57)`, `variables (3 / 11)` | parser/runtime dependencies |
| `P2` | `aliases`, `command`, `conditions`, `effects`, `entity`, `events`, `expressions`, `literals`, `localization` | import after core closure |
| `P3` | `bukkitutil`, `doc`, `hooks`, `test`, `timings`, `update` | defer |

## Reclassification Policy

- The exact-path tracker stays in place for bookkeeping, but remaining files are no longer all treated as equal-priority parity work.
- `Must Port`
  - Remaining user-visible Skript surface that should still exist on Fabric.
  - Current cluster: the remaining expression backlog, remaining sections, literals, arithmetic support, and parser/value families.
- `Adapt`
  - User-visible surfaces that need Fabric-native equivalents rather than literal Bukkit/Paper class parity.
  - Current cluster: aliases, command surface, serializer/storage/config glue, slot/util wrappers, and Bukkit-shaped expressions such as chat/playerlist/server-icon/plugin-state/command metadata/teleport-cause/spawn-reason.
- `Non-goal`
  - Upstream files that are not worth reproducing on Fabric unless they become direct blockers.
  - Current cluster: `bukkitutil`, `hooks`, `test`, `doc`, `timings`, `update`, bridge/tooling files, `StructAutoReload`, and explicit exclusions such as `ExprPlugins`.

## Active Blockers

### `Part 1A`

- Parser flow:
  - landed: shared matcher, tags/marks, omitted-placeholder defaults, `InputSource`, prefixed variables, legacy `parseStatic` flag parity
  - open: broader pattern graph/runtime parity and broader default-value parity
- Statement and loader flow:
  - landed: retained diagnostics, section fallback, plain-effect section ownership, unreachable-code warnings, hint scopes, comment-aware parsing
  - open: broader orchestration only when a concrete mismatch is reproduced
- Function runtime:
  - landed: local-first lookup, exact-type overload preference, parsed default parameters, keyed plural default behavior
  - landed: `StructFunction` declaration loading plus runtime `return` registration
  - open: broader namespace/default/runtime parity

### `Part 1B`

- Variable runtime:
  - landed: case-insensitive storage, list reindexing, natural numeric ordering, raw nested-map reads, hint bridges
  - open: still far from upstream-complete runtime behavior
- Type and parse registry:
  - landed: codename/literal/supertype lookup, class ordering, explicit-literal-only pattern lookup, converters, default-expression helpers
  - open: compatibility layer is still much thinner than upstream

### `Part 2`

- Exact runtime imports already landed:
  - alive/dead
  - silent
  - invulnerable
  - `feed`
  - invisible/visible
  - burning/on-fire
  - AI
  - sprinting
  - glowing
  - lane B server/session slice:
    - `ExprMOTD`
    - `ExprOnlinePlayersCount`
    - `ExprOps`
    - `ExprVersion`
    - `ExprViewDistance`
    - `ExprWhitelist`
  - lane A vector/location slice:
    - `ExprLocationFromVector`
    - `ExprLocationVectorOffset`
    - `ExprMidpoint`
    - `ExprVectorBetweenLocations`
    - `ExprVectorCrossProduct`
    - `ExprVectorDotProduct`
    - `ExprVectorLength`
    - `ExprVectorNormalize`
    - `ExprXYZComponent`
    - `ExprYawPitch`
- Exact-path parity for conditions, effects, expressions, events, and sections is now closed (0 missing in each category except 2 Non-goal expression files).
- Exact-path parity is no longer enough to claim user-visible closure.
- Confirmed behavior-only gaps inside existing files now include:
  - previously missing simple-event registrations now all landed: `join`, `connect`, `kick`, `quit`, `jump`, `hand item swap`, `server list ping`
  - missing particle/game-effect class-info registration for typed script positions
  - narrowed event patterns in `EvtGameMode`, `EvtWeatherChange`, `EvtPlayerArmorChange`, `EvtClick`, `EvtHarvestBlock`, and `EvtResourcePackResponse`
  - dead command-surface branches such as `all script commands` and partial `command info`

## Part Tracker

| Part | Scope | Status |
| --- | --- | --- |
| `Part 0` | inventory and doc move | `completed` |
| `Part 1A` | `lang` parser/runtime closure | `in_progress` |
| `Part 1B` | dependency closure around parser/runtime | `in_progress` |
| `Part 2` | missing user-visible upstream syntax imports | `in_progress` |
| `Part 3` | low-priority support packages | `pending` |

## Latest Verified Merge

- Landed:
  - cycle F worker-first expression batch:
    - `ExprArgument`
    - `ExprParse`
    - `ExprParseError`
    - `ExprValue`
    - `ExprCommandInfo`
    - `ExprResult`
    - `ExprScript`
    - `ExprScriptsOld`
    - `ExprHexCode`
    - `ExprColorFromHexCode`
    - `ExprRecursiveSize`
    - `ExprBlockSphere`
    - `ExprMe`
    - `ExprTypeOf`
    - `ExprSkullOwner`
    - `ExprEnchantmentLevel`
    - `ExprEnchantments`
    - `ExprMaxMinecartSpeed`
    - `ExprMinecartDerailedFlyingVelocity`
    - `ExprCompassTarget`
    - `ExprPortal`
    - `LitConsole`
  - cycle K expressions `ExprElement`, `ExprLoopValue`, `ExprLowestHighestSolidBlock`, `ExprResonatingTime`, `ExprRingingTime`, and `ExprXOf`
  - cycle L expression `ExprProjectileForce` plus live bow-force payload propagation
  - cycle M expressions `ExprSkull`, `ExprSignText`, and `ExprSpawnerType`
  - bootstrap/binding closure for the cycle M expression bundle
  - cycle F compatibility/binding closure and real `.sk` GameTest entrypoint wiring for the landed safe1/safe2/safe4/safe5/safe6 bundles
- Deferred:
  - dropped cycle F worker slices `ExprCmdCooldownInfo`, `ExprEntities`, `ExprValueWithin`, and the safe3 section/literal lane
  - `PrivateFishingHookAccess.currentState`
- Added compatibility coverage:
  - `ExpressionCycle20260313FBindingCompatibilityTest`
  - `ExpressionCycle20260313FSafe1BindingCompatibilityTest`
  - `ExpressionCycle20260313FSafe1CompatibilityTest`
  - `ExpressionCycle20260313FSafe2BindingCompatibilityTest`
  - `ExpressionCycle20260313FSafe2CompatibilityTest`
  - `ExpressionCycle20260313FSafe4BindingCompatibilityTest`
  - `ExpressionCycle20260313FSafe4CompatibilityTest`
  - `ExpressionCycle20260313FSafe5CompatibilityTest`
  - `ExpressionCycle20260313FSafe6CompatibilityTest`
  - `ExpressionCycle20260313FSafe5BindingTest`
  - `SkriptFabricExpressionCycleFSafe1GameTest`
  - `SkriptFabricExpressionCycleFSafe4GameTest`
  - `SkriptFabricExpressionCycle20260313FSafe5GameTest`
  - `SkriptFabricExpressionCycleFSyntax1GameTest`
  - `ExpressionCycle20260313KCompatibilityTest`
  - `ExpressionCycle20260313KBindingCompatibilityTest`
  - `ExpressionCycle20260313LCompatibilityTest`
  - `ExpressionCycle20260313LBindingCompatibilityTest`
  - `ExpressionCycle20260313MCompatibilityTest`
  - `ExpressionCycle20260313MBindingCompatibilityTest`
  - `SkriptFabricExpressionCycleKSyntax1GameTest`
  - `SkriptFabricExpressionCycleKSyntax2GameTest`
  - `SkriptFabricExpressionCycleLSyntax1GameTest`
  - `SkriptFabricExpressionCycleMSyntax1GameTest`
- Verification refreshed on 2026-03-13:
  - targeted cycle JUnit suite covering cycle M compatibility and bootstrap/binding
  - cycle M real `.sk` GameTest entrypoint for skull, live sign text, and spawner-type mutation
  - `./gradlew runGameTest --rerun-tasks`
- Current runtime baseline after the refresh: `369 / 372` GameTests (3 pre-existing parse-diagnostic failures, 410 `.sk` script files)
