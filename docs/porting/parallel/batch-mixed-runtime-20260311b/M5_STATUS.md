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

- none beyond the `SkriptFabricAdditionalSyntax` force-initialization added in this lane

## Targeted Tests

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionBlockWorldLocationCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.WorldLocationExpressionSyntaxTest`
- direct compatibility coverage extended for parse/instantiate/change-contract checks in the block/world/location bundle
- isolated-registry coverage added to confirm bootstrap registration for the landed world/location expressions

## Blockers

- not landed in this lane: `ExprBlockSphere`, `ExprCompassTarget`, `ExprLocationFromVector`, `ExprLocationVectorOffset`, `ExprLowestHighestSolidBlock`, `ExprMidpoint`, `ExprSeaPickles`, `ExprSignText`, `ExprSourceBlock`, `ExprPortal`
- `ExprHumidity` needed reflective climate-settings access because the mapped server biome type in this runtime does not expose `Biome.downfall()` directly

## Merge Note

- preserves the existing partial worktree patch and avoids edits to canonical docs or `SkriptFabricBootstrap.java`
- ready to merge as the M5 world/property expression slice for the landed classes above; remaining assigned targets stay open for a follow-up lane
