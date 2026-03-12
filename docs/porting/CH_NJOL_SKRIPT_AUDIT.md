# `ch/njol/skript` Audit And Closure Plan

Last condensed: 2026-03-12
Baseline snapshot date: 2026-03-08

## Baseline

- Upstream snapshot: `e6ec744`
- Upstream `ch/njol/skript`: `1189` Java files
- Exact-path missing in local tree: `263`
- Exact-path expressions missing: `77`
- Exact-path events / sections / command / aliases missing: `0 / 8 / 9 / 9`

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
  - cycle I syntax expression `ExprNumbers`
  - bootstrap/binding closure for `ExprNumbers`
  - cycle I real `.sk` GameTest entrypoint wiring for `ExprNumbers`
- Deferred:
  - teleport-cause stayed out after the real-trigger GameTest still resolved a missing cause
  - spawn-reason stayed out after the worker could not produce a GameTest-clean commit
  - `ExprReadiedArrow`, `ExprAppliedEffect`, weather-change runtime dispatch, and `PrivateFishingHookAccess.currentState` stayed out this cycle
- Added compatibility coverage:
  - `ExpressionCycle20260312ISyntax1CompatibilityTest`
  - `ExpressionCycle20260312IBindingCompatibilityTest`
  - `SkriptFabricExpressionCycleISyntax1GameTest`
- Verification refreshed on 2026-03-13:
  - targeted cycle JUnit suite covering syntax1 I and bootstrap/binding I
  - cycle I real `.sk` GameTest entrypoint for `ExprNumbers`
  - `./gradlew runGameTest --rerun-tasks`
- Current runtime baseline after the refresh: `310` GameTests executed, with `ExprNumbers` cleared and one unrelated beacon runtime-lock failure remaining
