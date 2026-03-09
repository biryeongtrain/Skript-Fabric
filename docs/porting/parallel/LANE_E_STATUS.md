# Lane E Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`
- package scaffolding only in owned `effects` / `events` / `entity`

## Latest Slice

- restored the shared `expressions/base` scaffolding bundle with upstream-backed `PropertyExpression`, `SimplePropertyExpression`, and `WrapperExpression`
- expanded `conditions/base/PropertyCondition` from the local stub into the upstream-style generic base with legacy pattern generation, registration helpers, negation/init wiring, and singular/plural `toString(...)`
- added owned package descriptors for `expressions`, `expressions/base`, `conditions`, `conditions/base`, `effects`, `events`, and `entity`
- added narrow compatibility coverage for expression-base registration/forwarding/simplification and property-condition pattern/registration/`toString(...)` behavior

## Verification

- `./gradlew test --tests ch.njol.skript.expressions.base.ExpressionBaseCompatibilityTest --tests ch.njol.skript.conditions.base.PropertyConditionCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is still blocked on missing cross-lane registration/event-value/entity infrastructure; stay on shared surface helpers or package-local scaffolding that does not require new `registrations`, parser, or Bukkit bridge work

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/base/PropertyCondition.java`
  - `src/main/java/ch/njol/skript/expressions/base/*`
  - `src/test/java/ch/njol/skript/conditions/base/PropertyConditionCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/expressions/base/ExpressionBaseCompatibilityTest.java`
