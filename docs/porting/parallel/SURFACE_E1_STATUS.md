# Surface E1 Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/RecoveredExpressionBundleSyntaxTest.java`

## Latest Slice

- recovered an 11-class entity-property expression bundle on the current coordinator base:
  - `ExprAI`
  - `ExprAttackCooldown`
  - `ExprExhaustion`
  - `ExprFallDistance`
  - `ExprFireTicks`
  - `ExprFlightMode`
  - `ExprFreezeTicks`
  - `ExprGravity`
  - `ExprLastDamage`
  - `ExprLevelProgress`
  - `ExprMaxFreezeTicks`
- kept upstream `ch.njol.skript.doc.*` annotations on the imported syntax classes
- aligned the bundle to the local runtime by replacing missing array helpers and using local parser/bootstrap paths inside the targeted test
- registered the recovered expressions through `SkriptFabricBootstrap`

## Verification

- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.RecoveredExpressionBundleSyntaxTest --rerun-tasks`
  - passed

## Next Lead

- stay in nearby expression/condition closures that do not need `FabricLocation` / `FabricBlock` compat decisions

## Merge Notes

- likely conflicts:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/main/java/ch/njol/skript/expressions/**`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/RecoveredExpressionBundleSyntaxTest.java`
