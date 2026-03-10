# M5 Status

## Scope

- `expressions/**` block/world/location bundle only
- narrow tests for assigned expressions

## Assigned Targets

- `20` expressions:
  - `ExprAbsorbedBlocks`
  - `ExprAltitude`
  - `ExprAttachedBlock`
  - `ExprBed`
  - `ExprBiome`
  - `ExprBlock`
  - `ExprBlockData`
  - `ExprBlockSound`
  - `ExprBlocks`
  - `ExprChunk`
  - `ExprCoordinate`
  - `ExprDifficulty`
  - `ExprDirection`
  - `ExprDistance`
  - `ExprDustedStage`
  - `ExprFacing`
  - `ExprLightLevel`
  - `ExprMiddleOfLocation`
  - `ExprMoonPhase`
  - `ExprPushedBlocks`

## Landed Classes

- Fabric-adapted or newly landed:
  - `ExprAltitude`
  - `ExprAttachedBlock`
  - `ExprBiome`
  - `ExprBlock`
  - `ExprBlockData`
  - `ExprBlockSound`
  - `ExprBlocks`
  - `ExprChunk`
  - `ExprCoordinate`
  - `ExprDifficulty`
  - `ExprDirection`
  - `ExprDistance`
  - `ExprDustedStage`
  - `ExprFacing`
  - `ExprLightLevel`
  - `ExprMiddleOfLocation`
  - `ExprMoonPhase`
- Minimal helper glue used by the same bundle:
  - `ch.njol.skript.util.Direction`
  - `ch.njol.skript.util.AABB`
  - `ch.njol.skript.util.BlockLineIterator`
  - `ch.njol.skript.util.MoonPhase`

## Runtime-Eligible Classes

- Verified in the direct FabricLocation/FabricBlock/ServerLevel-compatible subset:
  - `ExprAltitude`
  - `ExprAttachedBlock`
  - `ExprBiome`
  - `ExprBlock`
  - `ExprBlockData`
  - `ExprBlockSound`
  - `ExprBlocks`
  - `ExprChunk`
  - `ExprCoordinate`
  - `ExprDifficulty`
  - `ExprDirection`
  - `ExprDistance`
  - `ExprDustedStage`
  - `ExprFacing`
  - `ExprLightLevel`
  - `ExprMiddleOfLocation`
  - `ExprMoonPhase`

## Bootstrap Registrations Needed

- none in this lane
- no `SkriptFabricBootstrap.java` edits were made
- syntax registration remains class-static inside the landed expression classes

## Targeted Tests

- `./gradlew compileJava --console=plain -x test`
  - result: `BUILD SUCCESSFUL`
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionBlockWorldLocationCompatibilityTest --console=plain`
  - result: `BUILD SUCCESSFUL`
- targeted coverage exercised:
  - local `FabricLocation` math for `ExprAltitude`, `ExprCoordinate`, `ExprMiddleOfLocation`, `ExprDistance`
  - parser/runtime binding for `ExprAttachedBlock`, `ExprBiome`, `ExprBlockData`, `ExprBlockSound`, `ExprChunk`, `ExprDifficulty`, `ExprDirection`, `ExprDustedStage`, `ExprFacing`, `ExprLightLevel`, `ExprMoonPhase`

## Blockers

- `ExprAbsorbedBlocks`
  - no owned Fabric sponge-absorb runtime handle or `BlockStateBlock` compat equivalent is present in this lane
- `ExprBed`
  - upstream depends on Bukkit-style offline player bed spawn semantics, outside the assigned Fabric block/world/location subset
- `ExprPushedBlocks`
  - no owned piston movement runtime handle is present in this lane
- known `FabricLocation`/`FabricBlock` compat boundary did not block the direct subset; remaining blocked work requires event/runtime ownership outside this lane

## Merge Note

- Expect conflicts if another lane touched:
  - `src/main/java/ch/njol/skript/expressions/ExprBlock.java`
  - `src/main/java/ch/njol/skript/expressions/ExprBlocks.java`
  - `src/main/java/ch/njol/skript/expressions/ExprDirection.java`
  - `src/main/java/ch/njol/skript/util/Direction.java`
- Additional likely conflict points from the direct subset:
  - `src/main/java/ch/njol/skript/expressions/ExprBlockData.java`
  - `src/main/java/ch/njol/skript/expressions/ExprCoordinate.java`
  - `src/main/java/ch/njol/skript/expressions/ExprDifficulty.java`
  - `src/main/java/ch/njol/skript/expressions/ExprLightLevel.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionBlockWorldLocationCompatibilityTest.java`
- No bootstrap file edits were made here; coordinator can merge the Fabric-native subset without reviving the blocked event-only expressions.
