landed classes
- `ExprAltitude`
- `ExprBlock`
- `ExprBlockData`
- `ExprCoordinate`
- `ExprDifficulty`
- `ExprDistance`
- `ExprLightLevel`
- `ExprMiddleOfLocation`
- `FabricLocationExpressionSupport`
- `ExpressionBlockWorldLocationCompatibilityTest`

runtime-eligible classes
- `ExprAltitude` Fabric-native and runtime-safe once the expression is instantiated during bootstrap registration
- `ExprBlock` partially runtime-safe for event-block and block-at-location forms on existing `FabricBlockEventHandle` / `FabricLocation` compat
- `ExprBlockData` runtime-safe for `FabricBlock` reads and set operations once the syntax is made live
- `ExprCoordinate` parser-capable and change-capable for mutable `FabricLocation` expressions
- `ExprDifficulty` parser-capable on local `world` compat; runtime activation still needs a `difficulty` classinfo registration path
- `ExprDistance` Fabric-native and runtime-safe for same-world `FabricLocation` values
- `ExprLightLevel` Fabric-native for direct location lookups; deferred direction variants remain blocked on the missing `Direction` compat layer
- `ExprMiddleOfLocation` Fabric-native and runtime-safe

bootstrap registrations needed
- instantiate/register `ExprAltitude`
- instantiate/register `ExprBlock`
- instantiate/register `ExprBlockData`
- instantiate/register `ExprCoordinate`
- instantiate/register `ExprDifficulty`
- instantiate/register `ExprDistance`
- instantiate/register `ExprLightLevel`
- instantiate/register `ExprMiddleOfLocation`
- classinfo/runtime glue still needed before full activation for `%difficulty%` and `%blockdata%` on the shared bootstrap path

targeted tests
- attempted exact class filter: `./gradlew test --tests ch.njol.skript.expressions.ExpressionBlockWorldLocationCompatibilityTest --rerun-tasks`
  - Gradle compiled successfully but reported `No tests found for given includes`
- attempted exact wildcard filter: `./gradlew test --tests 'ch.njol.skript.expressions.ExpressionBlockWorldLocationCompatibilityTest*' --rerun-tasks`
  - Gradle compiled successfully but reported `No tests found for given includes`
- passed: `./gradlew test --tests 'ch.njol.skript.expressions.*' --rerun-tasks`

blockers
- known `FabricLocation` / `FabricBlock` compat boundary still blocks upstream-shape `ExprBiome`, `ExprBlocks`, `ExprChunk`, `ExprDirection`, and `ExprFacing`
- missing `Direction` compat blocks upstream directional forms for `ExprBlock`, `ExprLightLevel`, and the remaining assigned direction family
- missing chunk/biome type registrations block `ExprBiome` and `ExprChunk`
- missing event producers/handles block `ExprAbsorbedBlocks` and `ExprPushedBlocks`
- Bukkit/Paper-only APIs block `ExprAttachedBlock`, `ExprBed`, and `ExprMoonPhase`
- `ExprDustedStage` needs a dedicated Fabric brushable-block adaptation instead of the upstream Bukkit `Brushable` path

merge-note
- likely conflict surface is `src/main/java/ch/njol/skript/expressions/**`
- no `SkriptFabricBootstrap.java` edits
