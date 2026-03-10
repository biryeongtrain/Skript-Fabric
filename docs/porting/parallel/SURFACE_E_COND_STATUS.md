# Surface E Conditions Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/conditions/**`
- tightly matching tests only

## Latest Slice

- landed 13 more upstream-backed condition imports/adaptations on the current Fabric compat surface:
  - `CondCanSee`
  - `CondGlowingText`
  - `CondIsLoaded`
  - `CondIsPathfinding`
  - `CondIsRiding`
  - `CondIsRinging`
  - `CondIsSaddled`
  - `CondPlayedBefore`
  - `CondTooltip`
  - `CondCanHold`
  - `CondIsStackable`
  - `CondIsWithin`
  - `CondWithinRadius`
- added `ConditionSurfaceEConditionCompatibilityTest` covering constructor reachability, legacy `toString(...)` shapes, and focused runtime checks for tooltip, hold-space, stackability, and radius behavior
- preserved upstream doc annotations on the newly restored condition classes
- kept these out of the closure because the local surface still does not expose the needed runtime/event support:
  - `CondHasClientWeather`
  - `CondHasResourcePack`
  - `CondIncendiary`
  - `CondIsFuel`
  - `CondWillHatch`
  - `CondLeashWillDrop`
  - `CondIsEnchanted`
- `CondHasClientWeather` and `CondHasResourcePack` need player-state tracking that is not currently surfaced through the Fabric runtime
- `CondIsFuel` still needs a stable zero-context hook into the server's current `FuelValues` surface before it can match the upstream condition cleanly
- `CondIncendiary`, `CondWillHatch`, and `CondLeashWillDrop` still need local event/runtime handles equivalent to the upstream Bukkit events
- `CondIsEnchanted` is still blocked by the missing local `EnchantmentType` compat class and its parser type registration

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
