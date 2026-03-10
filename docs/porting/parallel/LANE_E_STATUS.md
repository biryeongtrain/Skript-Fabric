# Lane E Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- imported `ExprAmount`, `ExprFormatDate`, `ExprIndices`, and `ExprInverse` as a pure-local expression utility closure
- kept the slice off aliases, container closures, Bukkit-only APIs, and new external libraries
- prior `CondPermission` runtime syntax remains merged underneath this helper batch

## Verification

- `./gradlew test --tests ch.njol.skript.conditions.CondPermissionCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.PermissionSyntaxTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionDateCompatibilityTest --tests ch.njol.skript.expressions.ExpressionDateCompatibilityTest --tests ch.njol.skript.expressions.ExpressionUnixDateCompatibilityTest --tests ch.njol.skript.conditions.ConditionBundleCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionValueCompatibilityTest --tests ch.njol.skript.expressions.ExpressionValueCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is still the nearby low-dependency expression/condition surface that avoids aliases or inventory/container crossings; `CondContains` still looks worse than adjacent bundles because it pulls aliases/container logic back into scope, so the next low-risk follow-up after this helper batch is another pure-local numeric/text cluster before container-aware conditions

## Merge Notes

- likely conflicts:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/main/java/ch/njol/skript/lang/SimplifiedCondition.java`
  - `src/main/java/ch/njol/skript/Skript.java`
