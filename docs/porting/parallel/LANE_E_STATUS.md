# Lane E Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- recovered the uncommitted fallback-heavy condition bundle under `src/main/java/ch/njol/skript/conditions/**` with only narrow API-alignment fixes needed for mergeability:
  - replaced removed `NameAndId` wrappers with direct `GameProfile` ban/op/whitelist checks
  - switched server whitelist mode lookup to `PlayerList`
  - mapped connected-state checks onto `ServerPlayer.hasDisconnected()`
  - mapped unbreakable checks onto `DataComponents.UNBREAKABLE`
- added one focused compatibility test covering the restored `breakable`/`unbreakable` condition branch and bootstrapped Minecraft in that test class for item registry access
- imported a mixed closure around nearby low-dependency condition/expression compatibility shims:
  - `CondAI`, `CondCompare`, `CondIsAlive`, `CondIsBurning`, `CondIsEmpty`, `CondIsInvisible`, `CondIsInvulnerable`, `CondIsSilent`, `CondIsSprinting`
  - `ExprGlowing`, `ExprRandom`, `ExprRandomCharacter`, `ExprTimes`
- registered `ExprRandomCharacter` and `ExprTimes` on the Fabric bootstrap after runtime parsing passed
- left `ExprRandom` support-surface only after `%*classinfo%` runtime parsing still misresolved `"string"` through the item-type path during init

## Verification

- `./gradlew test --tests ch.njol.skript.conditions.ConditionBundleCompatibilityTest`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.CondPermissionCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.PermissionSyntaxTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionDateCompatibilityTest --tests ch.njol.skript.expressions.ExpressionDateCompatibilityTest --tests ch.njol.skript.expressions.ExpressionUnixDateCompatibilityTest --tests ch.njol.skript.conditions.ConditionBundleCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionValueCompatibilityTest --tests ch.njol.skript.expressions.ExpressionValueCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionEffectClosureCompatibilityTest --tests ch.njol.skript.expressions.ExpressionClosureCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.RandomExpressionSyntaxTest --rerun-tasks`
  - passed
- `./gradlew build --rerun-tasks`
  - passed

## Next Lead

- next Lane E lead is still the nearby low-dependency expression/condition surface that avoids aliases or inventory/container crossings; retry `ExprRandom` runtime registration only after the `%*classinfo%` parser path is fixed, and keep an eye on server-backed condition bundles like ban/op/whitelist/online for runtime semantics because this recovery only verified compile alignment plus the unbreakable branch

## Merge Notes

- likely conflicts:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/main/java/ch/njol/skript/conditions/**`
  - `src/main/java/ch/njol/skript/expressions/**`
  - `src/test/java/ch/njol/skript/conditions/ConditionBundleCompatibilityTest.java`
