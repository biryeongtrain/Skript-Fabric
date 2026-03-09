# Lane E Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- landed a text/collection expression bundle plus matching string conditions on top of the earlier shared base helpers
- restored `ExprAlphabetList`, `ExprLength`, `ExprNumberOfCharacters`, `ExprRandomUUID`, `ExprSortedList`, `ExprSubstring`, and `ExprWhether`
- restored `CondIsSet`, `CondMatches`, and `CondStartsEndsWith`
- added focused bundle coverage in `ConditionBundleCompatibilityTest` and `ExpressionTextCollectionCompatibilityTest`

## Verification

- `./gradlew test --tests ch.njol.skript.expressions.base.ExpressionBaseCompatibilityTest --tests ch.njol.skript.conditions.base.PropertyConditionCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.conditions.ConditionBundleCompatibilityTest --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is the next low-dependency `expressions` / `conditions` cluster that rides on the shared base helpers without crossing into Lane F runtime/event ownership

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/base/PropertyCondition.java`
  - `src/main/java/ch/njol/skript/expressions/base/*`
  - `src/test/java/ch/njol/skript/conditions/base/PropertyConditionCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/expressions/base/ExpressionBaseCompatibilityTest.java`
  - `src/main/java/ch/njol/skript/conditions/Cond*.java`
  - `src/main/java/ch/njol/skript/expressions/Expr*.java`
