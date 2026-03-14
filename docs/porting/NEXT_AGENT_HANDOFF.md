# Next Agent Handoff

Last condensed: 2026-03-14
Last full verification: 2026-03-14

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
- Runtime-backed `Evt*.java`: `52 / 53`
- Synthetic/partial `Evt*.java`: `0 / 53`
- Non-runtime/manual `Evt*.java`: `1 / 53`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Upstream `ch/njol/skript` baseline: exact-path present `961`, upstream `1189`, shortfall `228`
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.expressions.ExpressionCycle20260313FBindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe1CompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe1BindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe2CompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe2BindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe4CompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe4BindingCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe5CompatibilityTest --tests org.skriptlang.skript.fabric.runtime.ExpressionCycle20260313FSafe5BindingTest --tests ch.njol.skript.expressions.ExpressionCycle20260313FSafe6CompatibilityTest --warning-mode none --console=plain` passed
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `340 / 340` GameTests green on `main`

## Most Recent Merged Slice

- cycle F lands worker-first proven subsets:
  - safe1: `ExprArgument`, `ExprParse`, `ExprParseError`, `ExprValue`
  - safe2: `ExprCommandInfo`, `ExprResult`, `ExprScript`, `ExprScriptsOld`
  - safe4: `ExprHexCode`, `ExprColorFromHexCode`, `ExprRecursiveSize`, `ExprBlockSphere`
  - safe5: `ExprMe`, `ExprTypeOf`, `ExprSkullOwner`, `ExprEnchantmentLevel`, `ExprEnchantments`
  - safe6: `ExprMaxMinecartSpeed`, `ExprMinecartDerailedFlyingVelocity`, `ExprCompassTarget`, `ExprPortal`, `LitConsole`
- cycle F drops unproven worker slices `ExprCmdCooldownInfo`, `ExprEntities`, `ExprValueWithin`, and the safe3 section/literal lane
- runtime bootstrap now force-initializes the landed cycle F expression bundles during full GameTest startup
- cycle F adds dedicated compatibility/binding JUnit plus real `.sk` GameTests for every surviving landed lane
- `StructFunction` is now landed; declared global/local functions load through the normal structure pipeline and execute in runtime GameTest

## Do Next

- Event-hook closure for runtime-backed `Evt*.java` is complete; keep docs and tests aligned with `52 / 53` live and `1 / 53` non-runtime/manual.
- Event-facing synthetic alias cleanup is also closed for the remaining hanging payload case; do not reintroduce `gametest ...` event aliases where public syntax plus real producer already exist.
- Full-suite stabilization is closed for the current baseline.
- Behavior-gap audit is now active in parallel with the exact-path tracker; do not assume an existing file means the upstream user-visible syntax actually works.
- Highest-impact confirmed unusable surfaces:
  - typed particle/game-effect positions such as function parameters
  - `command /...:`
  - `aliases:`
  - `auto reload`
- Confirmed narrowed event syntaxes:
  - `EvtGameMode`, `EvtWeatherChange`, `EvtPlayerArmorChange`, `EvtClick`, `EvtHarvestBlock`, `EvtResourcePackResponse`
  - omitted simple-event registrations: `Jump`, `Hand Item Swap`, `Server List Ping`
- Confirmed command-surface behavior gaps:
  - `all script commands` returns nothing
  - `is a skript command` checks Brigadier root commands, not script-defined commands
  - command info readers still return `null` for several upstream fields
- Resume from the `Must Port` bucket rather than treating all `228` exact-path leftovers as equal-priority work.
- Keep `Adapt` work scoped to Fabric-native replacements for user-visible behavior, not literal class parity.
- Treat `Non-goal` leftovers as excluded from normal closure planning unless one becomes a direct blocker.
- Immediate backlog remains expressions-first, with focus on the remaining user-visible expression bucket now that cycle F cut expressions missing from `65` to `44`.
- Keep `PrivateFishingHookAccess.currentState` out until the accessor target is corrected and revalidated in GameTest.
- Keep Stage 8 package counts unchanged unless you actually audit another package.
- If user-visible `.sk` behavior changes, add real `.sk` coverage and rerun GameTests.

## Working Bucket Guide

- `Must Port`
  - Core user-visible syntax and the runtime support that directly enables it.
  - Start with the remaining expression families, sections, and literals.
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
