# Lane E Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- landed the next low-dependency string/value bundle on top of the shared expression helpers
- restored `ExprJoinSplit`, `ExprIndicesOfValue`, `ExprDefaultValue`, and `CondAlphanumeric`
- expanded `ExpressionTextCollectionCompatibilityTest` to cover join/split behavior, default-value fallback, string/list position lookup, and keyed index lookup
- expanded `ConditionBundleCompatibilityTest` to cover positive and negated alphanumeric checks

## Verification

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --tests ch.njol.skript.conditions.ConditionBundleCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is the remaining low-dependency collection/string/value cluster that still avoids new runtime ownership crossings; `CondContains` still looks worse than adjacent expression imports because it drags aliases/inventory/container surface back into scope

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/CondAlphanumeric.java`
  - `src/main/java/ch/njol/skript/expressions/ExprDefaultValue.java`
  - `src/main/java/ch/njol/skript/expressions/ExprIndicesOfValue.java`
  - `src/main/java/ch/njol/skript/expressions/ExprJoinSplit.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionTextCollectionCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/conditions/ConditionBundleCompatibilityTest.java`
