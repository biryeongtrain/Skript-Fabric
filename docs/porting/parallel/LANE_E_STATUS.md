# Lane E Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- closed the nearest low-dependency date/time bundle after the text-character slice
- restored `CondDate`, `CondPastFuture`, `ExprNow`, `ExprDateAgoLater`, `ExprTimeSince`, `ExprUnixDate`, and `ExprUnixTicks`
- restored `SimplifiedCondition.fromCondition(...)` so imported constant conditions can collapse the same way as upstream in this compatibility layer

## Verification

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionDateCompatibilityTest --tests ch.njol.skript.expressions.ExpressionDateCompatibilityTest --tests ch.njol.skript.expressions.ExpressionUnixDateCompatibilityTest --tests ch.njol.skript.conditions.ConditionBundleCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is still the nearby low-dependency expression/condition surface that avoids aliases or inventory/container crossings; `CondContains` still looks worse than adjacent bundles because it pulls aliases/container logic back into scope, so the next low-risk follow-up is another standalone value/date/text helper before container-aware conditions

## Merge Notes

- likely conflicts:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/main/java/ch/njol/skript/lang/SimplifiedCondition.java`
