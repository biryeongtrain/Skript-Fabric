# `ch/njol/skript` Audit And Closure Plan

Last condensed: 2026-03-12
Baseline snapshot date: 2026-03-08

## Baseline

- Upstream snapshot: `e6ec744`
- Upstream `ch/njol/skript`: `1189` Java files
- Exact-path missing in local tree: `261`
- Exact-path expressions missing: `75`
- Exact-path events / sections / command / aliases missing: `0 / 8 / 9 / 9`

## Priority Matrix

| Tier | Packages | Why |
| --- | --- | --- |
| `P0` | `lang (81 / 85)` | closest numerically to upstream, still behavior-incomplete |
| `P1` | `classes (5 / 28)`, `config (6 / 20)`, `log (9 / 17)`, `patterns (13 / 14)`, `registrations (3 / 10)`, `sections (1 / 10)`, `structures (1 / 10)`, `util (8 / 57)`, `variables (3 / 11)` | parser/runtime dependencies |
| `P2` | `aliases`, `command`, `conditions`, `effects`, `entity`, `events`, `expressions`, `literals`, `localization` | import after core closure |
| `P3` | `bukkitutil`, `doc`, `hooks`, `test`, `timings`, `update` | defer |

## Reclassification Policy

- The exact-path tracker stays in place for bookkeeping, but remaining files are no longer all treated as equal-priority parity work.
- `Must Port`
  - Remaining user-visible Skript surface that should still exist on Fabric.
  - Current cluster: the remaining expression backlog, remaining sections, literals, arithmetic support, parser/value families, and `StructFunction`.
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
  - cycle J expressions `ExprAppliedEffect`, `ExprNearestEntity`, and `ExprTargetedBlock`
  - bootstrap/binding closure for the cycle J expression bundle
  - cycle J real `.sk` GameTest entrypoint wiring for applied-effect, nearest-entity, and targeted-block proof
- Deferred:
  - parser-heavy expression closure such as `ExprArgument`, `ExprParse`, `ExprParseError`, `ExprValue`, and `ExprValueWithin`
  - `PrivateFishingHookAccess.currentState`
- Added compatibility coverage:
  - `ExpressionCycle20260313JCompatibilityTest`
  - `ExpressionCycle20260313JBindingCompatibilityTest`
  - `SkriptFabricExpressionCycleJSyntax1GameTest`
- Verification refreshed on 2026-03-13:
  - targeted cycle JUnit suite covering cycle J compatibility and bootstrap/binding
  - cycle J real `.sk` GameTest entrypoint for applied-effect, nearest-entity, and targeted-block proof
  - `./gradlew runGameTest --rerun-tasks`
- Current runtime baseline after the refresh: `313 / 313` GameTests green on `main`
