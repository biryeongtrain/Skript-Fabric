# M1 Status

## Scope

- `src/gametest/**` for real producer-path event verification
- no canonical doc edits
- no bootstrap/runtime registration edits unless a producer-path failure forces them

## Assigned Targets

- primary concrete-event activation proof:
  - `EvtGrow`
  - `EvtPlantGrowth`
- fallback:
  - none in this shard; stop after a real producer-path proof or a hard blocker

## Landed Classes

- no new imported classes
- live-activation proof added for existing upstream event classes:
  - `EvtGrow`
  - `EvtPlantGrowth`

## Runtime-Eligible Classes

- `EvtGrow`
- `EvtPlantGrowth`

## Bootstrap Registrations Needed

- no worker-side bootstrap edits
- coordinator can promote both events to active runtime status after merge because production bootstrap already loads them through `SkriptFabricBootstrap -> SkriptFabricAdditionalSyntax`

## Targeted Tests

- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.EventBridgeBindingTest --rerun-tasks`
  - passed
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - suite startup reported `246` Fabric GameTests

## Blockers

- none

## Merge Note

- adds a real `.sk` GameTest that grows a live wheat crop through the actual `CropBlock.performBonemeal(...)` producer path
- keeps the older manual compat-dispatch GameTest intact as a narrower bridge regression
