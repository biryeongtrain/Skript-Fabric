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
- upstream `ch/njol/skript` snapshot: local `440 / 1189`, shortfall `749`
- immediate priority: reduce the raw `ch/njol/skript` shortfall by closing upstream package bundles, not polishing already-landed syntax

## Local Upstream Reference

Use local upstream sources only. Do not browse.

- `/tmp/skript-upstream-e6ec744-2`
- `/tmp/upstream-skript`

## Latest Closed Slice

- current verified head adds a 47-class runtime-facing closure:
  - `conditions`: `CondCanFly`, `CondCanPickUpItems`, `CondHasScoreboardTag`, `CondIsBlocking`, `CondIsClimbing`, `CondIsFlying`, `CondIsGliding`, `CondIsHandRaised`, `CondIsLeftHanded`, `CondIsOnGround`, `CondIsRiptiding`, `CondIsSleeping`, `CondIsSneaking`, `CondIsSwimming`, `CondIsTamed`, `CondIsBlock`, `CondIsBlockRedstonePowered`, `CondIsCommandBlockConditional`, `CondIsEdible`, `CondIsFlammable`, `CondIsInfinite`, `CondIsInteractable`, `CondIsOccluding`, `CondIsPassable`, `CondIsSolid`, `CondIsTransparent`, `CondIsVectorNormalized`
  - `effects`: `EffCustomName`, `EffEating`, `EffHandedness`, `EffIgnite`, `EffLeash`, `EffMakeFly`, `EffPlayingDead`, `EffShear`, `EffTame`, `EffToggleCanPickUpItems`, `EffActionBar`, `EffBroadcast`, `EffKick`, `EffMessage`, `EffOp`, `EffPlaySound`, `EffResetTitle`, `EffSendResourcePack`, `EffSendTitle`, `EffStopSound`
- latest verified full run remains `./gradlew build --rerun-tasks`
- the latest focused follow-up keeps the existing `230 / 230` baseline while reducing the raw shortfall to `749`
- imported syntax classes in this batch keep upstream `doc` annotations
- no missing-library rollback was needed in this slice; `x2` was intentionally left as a no-op because the location/world expression closure is structurally blocked on `FabricLocation` / `FabricBlock` versus upstream Bukkit types
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
2. next `effects` + `events` bundle after the new control-flow effect slice, still avoiding large new `org/...` runtime edits
3. next `expressions` + `conditions` bundle after this larger condition/effect closure, but keep the location/world expression family deferred until there is an explicit compat-type decision around `FabricLocation` / `FabricBlock`
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
