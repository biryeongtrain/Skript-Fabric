# Next Agent Handoff

Last updated: 2026-03-10

## Read Order

1. [README.md](README.md)
2. this file
3. [PORTING_STATUS.md](PORTING_STATUS.md)
4. [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
5. [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md) if running parallel workers
6. [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md) if running parallel workers

## Current Headline

- latest verified runtime baseline: `230 / 230`
- latest full verification:
  - `./gradlew build --rerun-tasks` passed
- Stage 8 package-local audit remains frozen at `23 / 214`
- upstream `ch/njol/skript` snapshot: local `662 / 1189`, shortfall `527`
- immediate priority: reduce the raw `ch/njol/skript` shortfall by closing upstream package bundles, not polishing already-landed syntax

## Local Upstream Reference

Use local upstream sources only. Do not browse.

- `/tmp/skript-upstream-e6ec744-2`
- `/tmp/upstream-skript`

## Latest Closed Slice

- latest verified syntax-core worker batch adds 40 upstream classes:
  - `conditions`: `CondEntityStorageIsFull`, `CondIsFuel`, `CondIsOfType`, `CondIsResonating`, `CondItemEnchantmentGlint`, `CondWillHatch`
  - `expressions`: `ExprAttacked`, `ExprAttacker`, `ExprCommandBlockCommand`, `ExprDamage`, `ExprDamageCause`, `ExprExperience`, `ExprFinalDamage`, `ExprHealReason`, `ExprItemCooldown`, `ExprLastDamageCause`
  - `effects`: `EffApplyBoneMeal`, `EffConnect`, `EffDetonate`, `EffEntityUnload`, `EffForceEnchantmentGlint`, `EffKeepInventory`, `EffLog`, `EffMakeSay`, `EffReplace`, `EffRun`, `EffScriptFile`, `EffSuppressTypeHints`, `EffSuppressWarnings`, `EffWorldBorderExpand`
  - `events`: `EvtBeaconEffect`, `EvtBeaconToggle`, `EvtBlock`, `EvtEntity`, `EvtEntityBlockChange`, `EvtGrow`, `EvtItem`, `EvtPlantGrowth`, `EvtPressurePlate`, `EvtVehicleCollision`
  - runtime-registered on `SkriptFabricBootstrap`:
    - conditions `6`, expressions `10`, effects `10`
  - import-only in this batch:
    - events `10`
    - effects `EffConnect`, `EffKeepInventory`, `EffMakeSay`, `EffScriptFile`
  - targeted verification:
    - `ConditionSyntaxS1CompatibilityTest`
    - `ExpressionCombatContextCompatibilityTest`
    - `ExpressionSyntaxS2CompatibilityTest` through `isolatedExpressionSyntaxS2CompatibilityTest`
    - `EffectMutationCompatibilityTest`
    - `EffectWorldServerCompatibilityTest`
  - final integration excluded `ExprFireworkEffect` and `EffExplosion`
- latest verified full run remains `./gradlew build --rerun-tasks`
- the latest focused follow-up keeps the existing `230 / 230` baseline while reducing the raw shortfall to `527`
- the active/import-only split is now explicit:
  - active runtime surface: conditions, expressions, and a 10-effect subset
  - import-only surface: the new event bundle plus `EffConnect`, `EffKeepInventory`, `EffMakeSay`, and `EffScriptFile`
- no missing-library rollback was needed in this slice
- the prior Lane E runtime/support surface (`CondPermission`, `CondIsDivisibleBy`, `CondMinecraftVersion`, `CondIsUsingFeature`, `ExprARGB`, `ExprAngle`, `ExprDebugInfo`, `ExprHash`, `ExprTimespanDetails`, `ExprAmount`, `ExprFormatDate`, `ExprIndices`, `ExprInverse`, `CondAI`, `CondCompare`, `CondIsAlive`, `CondIsBurning`, `CondIsEmpty`, `CondIsInvisible`, `CondIsInvulnerable`, `CondIsSilent`, `CondIsSprinting`, `ExprGlowing`, `ExprRandom`, `ExprRandomCharacter`, `ExprTimes`) remains merged underneath it

## Recent Closed Prereqs

These are already closed. Do not reopen without a new reproducer.

- legacy `parseStatic(...)` expression-placeholder flags
- explicit-literal-only `Classes.getPatternInfos(...)` candidate filtering
- case-sensitive classinfo lookup
- exact-type overload preference in `FunctionRegistry`
- split exact-overload ambiguity retention
- required omitted-placeholder fail-fast parsing

## Next Targets

1. remaining `variables` + `sections` + `structures` + `aliases` closure after the now-verified `LitEternity` / alias foundation follow-up, with the storage-backend / `FlatFileStorage` slice retried separately from the last reverted runtime regression
2. decide whether to bootstrap the syntax-core import-only remainder into the active Fabric runtime:
   - `events`: `EvtBeaconEffect`, `EvtBeaconToggle`, `EvtBlock`, `EvtEntity`, `EvtEntityBlockChange`, `EvtGrow`, `EvtItem`, `EvtPlantGrowth`, `EvtPressurePlate`, `EvtVehicleCollision`
   - `effects`: `EffConnect`, `EffKeepInventory`, `EffMakeSay`, `EffScriptFile`
3. next `expressions` + `conditions` bundle after the new syntax-core worker batch, but keep the location/world expression family deferred until there is an explicit compat-type decision around `FabricLocation` / `FabricBlock`
4. remaining `classes` / `registrations` follow-up after the pure-Java default class-data helpers, still avoiding `yggdrasil` or Bukkit data imports where possible
5. remaining `util` / `lang` blocker imports, especially `Direction` / `StructureType` and the next parser/runtime closure that unblocks `StructFunction`

## Parallel Defaults

- keep `Coordinator + 6 workers`
- worker reasoning default: `medium`
- use local upstream snapshot only
- one primary bundle plus one fallback bundle per lane
- if both still leave owned work open, continue into the next same-scope sub-bundle before stopping
- allow multiple commits per lane if they stay inside the owned bundle
- do not stop after the first small win; aim for at least `20` class-equivalent additions/restorations and preferably roughly `20-60`, or `2-4` verifiable commits unless the bundle is blocked or exhausted
- no web
- worker docs stay minimal
- lane split for the current phase:
  - `Lane A`: `classes` + `registrations` + `patterns`
  - `Lane B`: `config` + `util` + `localization`
  - `Lane C`: `variables` + `sections` + `structures` + `aliases` + `literals`
  - `Lane D`: `lang` + `log`
  - `Lane E`: `expressions` + `conditions`
  - `Lane F`: `effects` + `events` + `entity`

## Lane Status Format

Lane files under `docs/porting/parallel/` should stay short:

1. scope
2. latest slice
3. verification
4. next lead
5. merge notes

## Verification

Optional targeted verification while narrowing a lane:

```bash
./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks
```

Full verification:

```bash
./gradlew build --rerun-tasks
```

## Main Worktree Notes

Keep unrelated dirty files untouched in `/Users/qf/IdeaProjects/Skript-Fabric-port`:

- `.codex/environments/environment-2.toml`
- `.codex/environments/environment.toml`
- `scripts/`

## Transition Note

- there is an older syntax-import batch under `/private/tmp/skript-impl-20260309150545`
- do not let that legacy batch define the next phase
- close or park it cleanly, then relaunch workers under the new package-bundle ownership model
