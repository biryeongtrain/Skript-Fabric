# Next Agent Handoff

Last condensed: 2026-03-13
Last full verification: 2026-03-13

## Read Order

1. [README.md](README.md)
2. this file
3. [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
4. [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
5. [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
6. [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
7. [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md) and [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md) only for parallel runs

## Current State

- Source ports complete: conditions `28 / 28`, expressions `84 / 84`, effects `24 / 24`
- Runtime-backed `Evt*.java`: `48 / 53`
- Synthetic/partial `Evt*.java`: `0 / 53`
- Non-runtime/manual `Evt*.java`: `5 / 53`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Upstream `ch/njol/skript` baseline: exact-path present `938`, upstream `1189`, shortfall `251`
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.expressions.ExpressionCycle20260313MCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313MBindingCompatibilityTest --warning-mode none --console=plain` passed
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `319 / 319` GameTests green on `main`

## Most Recent Merged Slice

- cycle K adds upstream-exact `ExprElement`, `ExprLoopValue`, `ExprLowestHighestSolidBlock`, `ExprResonatingTime`, `ExprRingingTime`, and `ExprXOf`
- cycle L adds upstream-exact `ExprProjectileForce` and extends the live bow producer with projectile force payload
- cycle M adds `ExprSkull`, `ExprSignText`, and `ExprSpawnerType`
- runtime bootstrap now force-initializes the cycle M expression bundle during full GameTest startup
- cycle M adds dedicated compatibility/binding JUnit plus a real `.sk` GameTest for skull, live sign text, and spawner-type mutation

## Do Next

- Event-hook closure for runtime-backed `Evt*.java` is complete; keep docs and tests aligned with `48 / 53` live and `5 / 53` non-runtime/manual.
- Event-facing synthetic alias cleanup is also closed for the remaining hanging payload case; do not reintroduce `gametest ...` event aliases where public syntax plus real producer already exist.
- Full-suite stabilization is closed for the current baseline.
- Resume from the `Must Port` bucket rather than treating all `251` exact-path leftovers as equal-priority work.
- Keep `Adapt` work scoped to Fabric-native replacements for user-visible behavior, not literal class parity.
- Treat `Non-goal` leftovers as excluded from normal closure planning unless one becomes a direct blocker.
- Immediate backlog remains expressions-first, with focus on the remaining user-visible expression bucket.
- Keep `PrivateFishingHookAccess.currentState` out until the accessor target is corrected and revalidated in GameTest.
- Keep Stage 8 package counts unchanged unless you actually audit another package.
- If user-visible `.sk` behavior changes, add real `.sk` coverage and rerun GameTests.

## Working Bucket Guide

- `Must Port`
  - Core user-visible syntax and the runtime support that directly enables it.
  - Start with the remaining expression families, sections, literals, and `StructFunction`.
- `Adapt`
  - Bukkit-shaped but still user-visible surfaces where Fabric should expose equivalent behavior through a different implementation.
  - Includes aliases, command surface, storage/config glue, and chat/playerlist/server-icon/plugin-state style expressions.
- `Non-goal`
  - Bukkit utility, plugin hook, test harness, updater, doc generator, and bridge/tooling leftovers.
  - Keep them out of day-to-day closure counts and out of “parity complete” claims.

## Guardrails

- Do not claim parity complete.
- Do not re-expand these docs with long logs.
- Do not revive the 5-lane parallel doc expansion.
- Do not land syntax without a real `.sk` GameTest that proves parse and runtime behavior.
- Do not land event hooks whose only proof is direct compat-handle dispatch; use a real trigger and remove the replaced helper case from mixed backfill.
- Preserve exact counts and exact verification outcomes.
