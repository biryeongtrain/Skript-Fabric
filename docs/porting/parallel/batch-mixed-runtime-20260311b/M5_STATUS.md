# M5 Status

## Scope

- `expressions/**`
- minimal helper glue inside the same scope
- narrow tests for the assigned expression bundle

## Assigned Targets

- primary block/world/location bundle `12`:
  - `ExprBlockSphere`
  - `ExprCompassTarget`
  - `ExprHumidity`
  - `ExprLocation`
  - `ExprLocationAt`
  - `ExprLocationFromVector`
  - `ExprLocationOf`
  - `ExprLocationVectorOffset`
  - `ExprLowestHighestSolidBlock`
  - `ExprMidpoint`
  - `ExprRedstoneBlockPower`
  - `ExprSeaLevel`
- fallback location/world follow-up `8`:
  - `ExprSeaPickles`
  - `ExprSeed`
  - `ExprSignText`
  - `ExprSimulationDistance`
  - `ExprSourceBlock`
  - `ExprSpawn`
  - `ExprLocationVectorOffset` parity follow-up if still needed
  - `ExprPortal` or another adjacent world expression if the primary bundle blocks

## Landed Classes

- `ExprHumidity`
- `ExprLocation`
- `ExprLocationAt`
- `ExprLocationOf`
- `ExprRedstoneBlockPower`
- `ExprSeaLevel`
- `ExprSeed`
- `ExprSimulationDistance`
- `ExprSpawn`
- `ExprChunkX`
- `ExprChunkZ`
- helper follow-up: `FabricLocationExpressionSupport`

## Runtime-Eligible Classes

- `ExprHumidity`
- `ExprLocation`
- `ExprLocationAt`
- `ExprLocationOf`
- `ExprRedstoneBlockPower`
- `ExprSeaLevel`
- `ExprSeed`
- `ExprSimulationDistance`
- `ExprSpawn`
- `ExprChunkX`
- `ExprChunkZ`

## Bootstrap Registrations Needed

- coordinator deferred active registration in this batch
- lane is merged as import-only until representative real `.sk` GameTest coverage exists

## Targeted Tests

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionBlockWorldLocationCompatibilityTest`
- direct compatibility coverage extended for parse/instantiate/change-contract checks in the block/world/location bundle

## Blockers

- not landed in this lane: `ExprBlockSphere`, `ExprCompassTarget`, `ExprLocationFromVector`, `ExprLocationVectorOffset`, `ExprLowestHighestSolidBlock`, `ExprMidpoint`, `ExprSeaPickles`, `ExprSignText`, `ExprSourceBlock`, `ExprPortal`
- `ExprHumidity` needed reflective climate-settings access because the mapped server biome type in this runtime does not expose `Biome.downfall()` directly

## Merge Note

- preserves the existing partial worktree patch and avoids edits to canonical docs or `SkriptFabricBootstrap.java`
- coordinator kept this lane import-only in the merged branch because active runtime registration would require representative real `.sk` GameTest coverage
- remaining assigned targets stay open for a follow-up lane
