# Lane E Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- kept the low-dependency string/value bundle intact and moved into the adjacent numeric/chance bundle
- restored `CondChance` and `ExprRandomNumber`
- expanded `ConditionBundleCompatibilityTest` to cover percent, unit-interval, and `fails` chance forms with deterministic boundary values
- expanded `ExpressionTextCollectionCompatibilityTest` to cover single and plural random integers, fixed-bound doubles, and impossible integer-range handling

## Verification

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionBundleCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is still the nearby low-dependency expression/condition surface that avoids aliases or inventory/container crossings; `CondContains` still looks worse than adjacent bundles because it pulls that wider surface back into scope

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/CondChance.java`
  - `src/main/java/ch/njol/skript/expressions/ExprRandomNumber.java`
  - `src/main/java/ch/njol/skript/conditions/CondAlphanumeric.java`
  - `src/main/java/ch/njol/skript/expressions/ExprDefaultValue.java`
  - `src/main/java/ch/njol/skript/expressions/ExprIndicesOfValue.java`
  - `src/main/java/ch/njol/skript/expressions/ExprJoinSplit.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionTextCollectionCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/conditions/ConditionBundleCompatibilityTest.java`
