# `ch/njol/skript` Audit And Closure Plan

Last condensed: 2026-03-11
Baseline snapshot date: 2026-03-08

## Baseline

- Upstream snapshot: `e6ec744`
- Upstream `ch/njol/skript`: `1189` Java files
- Exact-path missing in local tree: `314`
- Exact-path expressions missing: `123`
- Exact-path events / sections / command / aliases missing: `5 / 8 / 9 / 9`

## Priority Matrix

| Tier | Packages | Why |
| --- | --- | --- |
| `P0` | `lang (81 / 85)` | closest numerically to upstream, still behavior-incomplete |
| `P1` | `classes (5 / 28)`, `config (6 / 20)`, `log (9 / 17)`, `patterns (13 / 14)`, `registrations (3 / 10)`, `sections (1 / 10)`, `structures (1 / 10)`, `util (8 / 57)`, `variables (3 / 11)` | parser/runtime dependencies |
| `P2` | `aliases`, `command`, `conditions`, `effects`, `entity`, `events`, `expressions`, `literals`, `localization` | import after core closure |
| `P3` | `bukkitutil`, `doc`, `hooks`, `test`, `timings`, `update` | defer |

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
- Most upstream condition/effect/expression families are still absent and stay behind `Part 1A` / `Part 1B`.

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
  - vector geometry expressions `ExprVectorAngleBetween`, `ExprVectorFromXYZ`, `ExprVectorOfLocation`, `ExprVectorProjection`, `ExprVectorRandom`, `ExprVectorSquaredLength`
  - world/time and border expressions `ExprTemperature`, `ExprTime`, `ExprWorld`, `ExprWorldEnvironment`, `ExprWorldFromName`, `ExprWorlds`, `ExprWorldBorder`, `ExprWorldBorderCenter`, `ExprWorldBorderSize`, `ExprWorldBorderDamageAmount`, `ExprWorldBorderDamageBuffer`, `ExprWorldBorderWarningDistance`
  - syntax3 property expressions `ExprUUID`, `ExprVelocity`, `ExprTimeLived`, `ExprScoreboardTags`, `ExprGameMode`, `ExprSaturation`
  - syntax4 relation expressions `ExprPassenger`, `ExprVehicle`, `ExprShooter`, `ExprTarget`, `ExprTransformReason`, `ExprUnleashReason`
  - scheduled and lifecycle event backends `EvtPeriodical`, `EvtAtTime`, `ExperienceSpawnEvent`, `ScriptEvent`, `SkriptStartEvent`, `SkriptStopEvent`
  - compat accessor migration for `PrivateBeaconAccess` and `PrivateBellAccess`
- Deferred:
  - `PrivateFishingHookAccess.currentState` migration stayed out after a GameTest mixin accessor failure
- Added compatibility coverage:
  - `ExpressionVectorGeometryCompatibilityTest`
  - `ExpressionSyntaxS2CompatibilityTest`
  - `ExpressionSyntaxS3CompatibilityTest`
  - `ExpressionSyntaxS4CompatibilityTest`
  - `ScheduledEventRuntimeTest`
  - `ExperienceSpawnRuntimeTest`
  - `ScriptLifecycleRuntimeTest`
- Verification passed on 2026-03-11:
  - targeted cycle JUnit suite covering vector geometry, syntax S2/S3/S4, mixed runtime binding, event compatibility, scheduled/experience/script lifecycle runtime, and compat accessor migration
  - `./gradlew runGameTest --rerun-tasks`
- Verified runtime baseline after that merge: `260 / 260`
