# Surface E Conditions Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/conditions/**`
- tightly matching tests only

## Latest Slice

- landed 12 upstream-backed condition imports adapted to the current Fabric compat surface:
  - `CondEndermanStaredAt`
  - `CondHasCustomModelData`
  - `CondHasLineOfSight`
  - `CondIsCharged`
  - `CondIsDancing`
  - `CondIsEating`
  - `CondIsFireResistant`
  - `CondIsJumping`
  - `CondIsPersistent`
  - `CondIsTicking`
  - `CondIsValid`
  - `CondLidState`
- added `ConditionClosureCompatibilityTest` covering constructor reachability, legacy `toString(...)` shapes, the fire-resistant item branch, `Validated` handling, and the player guard on `CondIsJumping`
- kept upstream doc annotations on all imported syntax classes
- intentionally left these out of this slice because they need non-owned runtime/event or missing support surfaces:
  - `CondCanSee`
  - `CondFromMobSpawner`
  - `CondIncendiary`
  - `CondLeashWillDrop`
  - `CondTooltip`
  - `CondWillHatch`
  - `CondIsEnchanted`

## Verification

- `./gradlew test --tests ch.njol.skript.conditions.ConditionClosureCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- if Lane E stays on conditions, the next clean retry is the remaining nearby bundle that can be mapped onto existing Fabric compat types without touching bootstrap or other lanes
- the skipped event-only conditions need a local event surface before import
- `CondIsEnchanted` still depends on a missing enchantment-type support class outside this lane-owned slice

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/**`
  - `src/test/java/ch/njol/skript/conditions/ConditionClosureCompatibilityTest.java`
- exact verification command:
  - `./gradlew test --tests ch.njol.skript.conditions.ConditionClosureCompatibilityTest --rerun-tasks`
