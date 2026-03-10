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
- upstream `ch/njol/skript` snapshot: local `727 / 1189`, shortfall `462`
- immediate priority: reduce the raw `ch/njol/skript` shortfall by closing upstream package bundles, not polishing already-landed syntax

## Local Upstream Reference

Use local upstream sources only. Do not browse.

- `/tmp/skript-upstream-e6ec744-2`
- `/tmp/upstream-skript`

## Latest Closed Slice

- latest verified mixed-runtime coordinator batch reduces the raw shortfall to `727 / 1189`:
  - live-activated events `11`:
    - `EvtBeaconEffect`, `EvtBeaconToggle`, `EvtBlock`, `EvtBookEdit`, `EvtBookSign`, `EvtClick`, `EvtEntity`, `EvtEntityTransform`, `EvtExperienceSpawn`, `EvtHealing`, `EvtItem`
  - newly imported runtime syntax:
    - conditions `10`: `CondCancelled`, `CondDamageCause`, `CondEntityUnload`, `CondIncendiary`, `CondItemDespawn`, `CondIsPreferredTool`, `CondIsSedated`, `CondLeashWillDrop`, `CondRespawnLocation`, `CondScriptLoaded`
    - expressions `20`: `ExprAffectedEntities`, `ExprBarterInput`, `ExprConsumedItem`, `ExprExperienceCooldownChangeReason`, `ExprExplodedBlocks`, `ExprHatchingNumber`, `ExprHatchingType`, `ExprHealAmount`, `ExprLastAttacker`, `ExprLeashHolder`, `ExprLevel`, `ExprMaxDurability`, `ExprMaxHealth`, `ExprMaxItemUseTime`, `ExprMaxStack`, `ExprNoDamageTicks`, `ExprItemOwner`, `ExprItemThrower`, `ExprRawName`, `ExprSpeed`
    - effects `19` unique: `EffColorItems`, `EffEnchant`, `EffEquip`, `EffDrop`, `EffHealth`, `EffTeleport`, `EffWakeupSleep`, `EffFireworkLaunch`, `EffElytraBoostConsume`, `EffExplosion`, `EffTree`, `EffEntityVisibility`, `EffClearEntityStorage`, `EffInsertEntityStorage`, `EffReleaseEntityStorage`, `EffCopy`, `EffSort`, `EffToggle`, `EffExceptionDebug`
  - runtime-registered on `SkriptFabricBootstrap`:
    - conditions `10`, expressions `20`, effects `4`, events `11`
  - import-only remainder from this batch:
    - `EffColorItems`, `EffEnchant`, `EffEquip`, `EffDrop`, `EffHealth`, `EffTeleport`, `EffWakeupSleep`, `EffFireworkLaunch`, `EffElytraBoostConsume`, `EffExplosion`, `EffTree`, `EffEntityVisibility`, `EffClearEntityStorage`, `EffInsertEntityStorage`, `EffReleaseEntityStorage`
  - coordinator stabilization:
    - `ExpressionEventContextBundleCompatibilityTest` isolated
    - `MixedRuntimeSyntaxBatchTest` added as an isolated runtime parser suite
    - healing compat handle now carries amount
- latest verified full run remains `./gradlew build --rerun-tasks`
- the latest focused follow-up keeps the existing `230 / 230` baseline while reducing the raw shortfall to `462`
- the active/import-only split is now explicit:
  - active runtime surface: conditions `10`, expressions `20`, effects `4`, events `11`
  - import-only surface: the blocked mixed-batch effect remainder plus the older `EffConnect`, `EffKeepInventory`, `EffMakeSay`, and `EffScriptFile`
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

1. live-activate the remaining imported concrete events that still have no runtime producer:
   - `EvtEntityBlockChange`, `EvtGrow`, `EvtPlantGrowth`, `EvtPressurePlate`, `EvtVehicleCollision`
2. broaden the partial active event classes instead of re-importing them:
   - `EvtBlock` beyond break-only backing
   - `EvtItem` beyond spawn-only backing
   - `EvtEntity` beyond spawn/death lifecycle backing
   - finer reason coverage for `EvtEntityTransform` and `EvtHealing`
3. resolve or continue the import-only mixed-batch effect remainder:
   - `EffColorItems`, `EffEnchant`, `EffEquip`, `EffDrop`, `EffHealth`, `EffTeleport`, `EffWakeupSleep`, `EffFireworkLaunch`, `EffElytraBoostConsume`, `EffExplosion`, `EffTree`, `EffEntityVisibility`, `EffClearEntityStorage`, `EffInsertEntityStorage`, `EffReleaseEntityStorage`
4. run the next syntax-heavy mixed-runtime worker batch for new `conditions` / `expressions` / `effects`, keeping worker targets at `10-20` syntax classes each and continuing to defer the location/world expression family until there is an explicit compat-type decision around `FabricLocation` / `FabricBlock`
5. after the next syntax batch, return to the remaining `variables` + `sections` + `structures` + `aliases` closure and the next `classes` / `util` / `lang` blocker imports

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
- for syntax-heavy runtime batches, prefer mixed-runtime sub-lanes instead of falling back to the old package A-F split:
  - `Lane 1`: event activation / remaining concrete event producers
  - `Lane 2`: remaining or adjacent condition closures
  - `Lane 3`: event-payload expressions
  - `Lane 4`: property / player expressions
  - `Lane 5`: item / entity mutation effects
  - `Lane 6`: utility / storage / server effects

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
