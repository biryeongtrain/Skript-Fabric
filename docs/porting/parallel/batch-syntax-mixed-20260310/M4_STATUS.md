# M4 Status

## landed classes
- `ExprLevel`
- `ExprMaxItemUseTime`
- `ExprMaxDurability`
- `ExprMaxHealth`
- `ExprMaxStack`
- `ExprNoDamageTicks`
- `ExprRawName`
- `ExprItemOwner`
- `ExprItemThrower`
- `ExprSpeed`

## runtime-eligible classes
- `ExprLevel`
- `ExprMaxItemUseTime`
- `ExprMaxDurability`
- `ExprMaxHealth`
- `ExprMaxStack`
- `ExprNoDamageTicks`
- `ExprRawName`
- `ExprItemOwner`
- `ExprItemThrower`
- `ExprSpeed`
- these are parser/unit-ready in this lane; live script availability still needs bootstrap registration outside this worker's allowed files

## bootstrap registrations needed
- add `register()` calls for:
  - `ExprLevel`
  - `ExprMaxItemUseTime`
  - `ExprMaxDurability`
  - `ExprMaxHealth`
  - `ExprMaxStack`
  - `ExprNoDamageTicks`
  - `ExprRawName`
  - `ExprItemOwner`
  - `ExprItemThrower`
  - `ExprSpeed`
- lane-local compat follow-up already included:
  - `FabricItemType.applyPrototype(...)` for item component mutation persistence

## targeted tests
- `./gradlew compileJava --rerun-tasks`
- result: passed
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionEntityCompatibilityTest --tests ch.njol.skript.expressions.ExpressionItemCompatibilityTest --rerun-tasks`
- result: passed
- `./gradlew isolatedExpressionSyntaxS2CompatibilityTest --rerun-tasks`
- result: passed

## blockers
- `ExprWeather`, `ExprLastDeathLocation`, and `ExprRespawnLocation` remain deferred because the location/world-expression family still depends on broader `FabricLocation` / world-surface decisions outside this lane
- fallback bundle classes `ExprName`, `ExprNamed`, and `ExprMaxMinecartSpeed` were not opened after the primary 10-class bundle landed and verified
- `ExprLevel` intentionally omits the upstream past/future event-time semantics because the relevant Fabric player-level event handle is not exposed for this package without widening runtime ownership
- `ExprItemThrower` lands with read/set support through current entity-owner resolution, but delete/reset-to-null semantics are still limited without a clearer lane-local thrower-clearing surface
- inventory max-stack mutation is still not available in the current compat layer; `ExprMaxStack` only mutates item types and rejects inventory changes

## merge-note
- likely conflicts:
  - `src/main/java/ch/njol/skript/expressions/*.java`
  - `src/main/java/org/skriptlang/skript/fabric/compat/FabricItemType.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionEntityCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionItemCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionSyntaxS2CompatibilityTest.java`
  - `docs/porting/parallel/batch-syntax-mixed-20260310/M4_STATUS.md`
- coordinator follow-up still needed in `SkriptFabricBootstrap.java` for all 10 expressions; this lane intentionally did not edit that file
