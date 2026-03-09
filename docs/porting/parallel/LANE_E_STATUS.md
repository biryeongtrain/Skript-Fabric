# Lane E Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- landed a follow-up collection/arithmetic expression bundle on top of the earlier shared base helpers
- restored `ExprAnyOf`, `ExprDifference`, `ExprExcept`, `ExprReversedList`, and `ExprShuffledList`
- expanded `ExpressionTextCollectionCompatibilityTest` to cover single-item narrowing, exclusion, reverse keyed iteration, shuffle keyed iteration, and arithmetic difference lookup

## Verification

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is the next low-dependency `expressions` / `conditions` cluster that rides on the shared base helpers without crossing into Lane F runtime/event ownership; arithmetic-heavy imports now look more reachable but still need careful registry overlap review

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/expressions/ExprAnyOf.java`
  - `src/main/java/ch/njol/skript/expressions/ExprDifference.java`
  - `src/main/java/ch/njol/skript/expressions/ExprExcept.java`
  - `src/main/java/ch/njol/skript/expressions/ExprReversedList.java`
  - `src/main/java/ch/njol/skript/expressions/ExprShuffledList.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionTextCollectionCompatibilityTest.java`
