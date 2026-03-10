# Lane E Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- closed the next pure-local helper bundle after the date/time slice without reintroducing Bukkit-bound surfaces
- restored `CondIsDivisibleBy`, `CondMinecraftVersion`, `CondIsUsingFeature`, `ExprARGB`, `ExprAngle`, `ExprDebugInfo`, `ExprHash`, and `ExprTimespanDetails`
- kept the slice off aliases, inventory/container, and default-runtime bootstrap edits

## Verification

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionDateCompatibilityTest --tests ch.njol.skript.expressions.ExpressionDateCompatibilityTest --tests ch.njol.skript.expressions.ExpressionUnixDateCompatibilityTest --tests ch.njol.skript.conditions.ConditionBundleCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionValueCompatibilityTest --tests ch.njol.skript.expressions.ExpressionValueCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is still the nearby low-dependency expression/condition surface that avoids aliases or inventory/container crossings; `CondContains` still looks worse than adjacent bundles because it pulls aliases/container logic back into scope, so the next low-risk follow-up is another pure-local numeric/text helper cluster before container-aware conditions

## Merge Notes

- likely conflicts:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/main/java/ch/njol/skript/lang/SimplifiedCondition.java`
  - `src/main/java/ch/njol/skript/Skript.java`
