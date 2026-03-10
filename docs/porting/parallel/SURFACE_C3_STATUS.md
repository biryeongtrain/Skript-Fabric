# Surface C3 Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/conditions/**`
- tightly matching tests only

## Latest Slice

- recovered the existing 16-class entity-behavior condition bundle already present under `ch/njol/skript/conditions`
- kept the written condition classes intact except for narrow local-runtime fixes required to compile on this branch:
  - `CondAllayCanDuplicate` now reads the private duplication flag through reflection
  - `CondEntityIsInLiquid` now uses reflective rain access plus block-state bubble-column detection
  - `CondEntityIsWet` now uses local block-state bubble-column detection
- retained the targeted `EntityBehaviorConditionCompatibilityTest` and used it as the sole verification surface for this recovery

## Verification

- `./gradlew test --tests ch.njol.skript.conditions.EntityBehaviorConditionCompatibilityTest --rerun-tasks`
  - first run failed at `:compileJava` because the imported bundle referenced inaccessible or missing local entity methods (`Allay.canDuplicate()`, `Entity.isInRain()`, `Entity.isInBubbleColumn()`)
- `./gradlew test --tests ch.njol.skript.conditions.EntityBehaviorConditionCompatibilityTest --rerun-tasks`
  - second run failed at `:test` because `CondAllayCanDuplicate` and `CondGoatHasHorns` still emitted default `BE` text forms instead of `CAN` / `HAVE`
- `./gradlew test --tests ch.njol.skript.conditions.EntityBehaviorConditionCompatibilityTest --rerun-tasks`
  - third run passed

## Next Lead

- coordinator merge should watch for adjacent conflicts in `ch/njol/skript/conditions/**` from other condition-surface workers

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/**`
  - `src/test/java/ch/njol/skript/conditions/EntityBehaviorConditionCompatibilityTest.java`
