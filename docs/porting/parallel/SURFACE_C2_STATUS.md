# Surface C2 Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/conditions/**`
- tightly matching tests only

## Latest Slice

- imported 12 upstream-backed condition classes into the local Fabric/net.minecraft compatibility surface:
  - `CondIsBlock`
  - `CondIsBlockRedstonePowered`
  - `CondIsCommandBlockConditional`
  - `CondIsEdible`
  - `CondIsFlammable`
  - `CondIsInfinite`
  - `CondIsInteractable`
  - `CondIsOccluding`
  - `CondIsPassable`
  - `CondIsSolid`
  - `CondIsTransparent`
  - `CondIsVectorNormalized`
- adapted the item/block/vector conditions from upstream Bukkit types onto local `FabricItemType`, `FabricBlock`, `Vec3`, and `Timespan`
- added `ConditionBlockItemCompatibilityTest` to cover the landed item/value conditions on the local surface
- intentionally did not land `CondContains`, `CondIsLoaded`, `CondIsWithin`, `CondWithinRadius`, or `CondIsSpawnable` because they still depend on missing or cross-lane support (`aliases`, `Direction`, `AABB`, world/script helpers, or entity runtime closure)
- attempted `CondIsFuel`, but dropped it after the local runtime surface still lacked a stable accessible fuel API path; this remains a blocker rather than a silent omission

## Verification

- `./gradlew test --tests ch.njol.skript.conditions.ConditionBlockItemCompatibilityTest --rerun-tasks`
  - first run failed at `:compileJava`
  - result: local main source set does not expose Bukkit types; rewrote the batch to Fabric/net.minecraft equivalents
- `./gradlew test --tests ch.njol.skript.conditions.ConditionBlockItemCompatibilityTest --rerun-tasks`
  - second run failed at `:compileJava`
  - result: adjusted `CondIsFuel` reflection path and `CondIsTransparent` method signature
- `./gradlew test --tests ch.njol.skript.conditions.ConditionBlockItemCompatibilityTest --rerun-tasks`
  - third run failed at `:test`
  - result: added Minecraft bootstrap in the test before touching `Items.*`
- `./gradlew test --tests ch.njol.skript.conditions.ConditionBlockItemCompatibilityTest --rerun-tasks`
  - fourth run failed at `:test`
  - result: removed `CondIsFuel` from the landed batch after the local fuel check still had no reliable API path
- `./gradlew test --tests ch.njol.skript.conditions.ConditionBlockItemCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- retry the remaining block/world conditions only after their local dependencies are present:
  - `CondIsLoaded` needs the missing `Direction` utility closure
  - `CondIsWithin` and `CondWithinRadius` need the missing local area/location helper closure
  - `CondContains` still crosses into alias/container support
  - `CondIsFuel` needs a confirmed local furnace-fuel API path before reintroduction

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/**`
  - `src/test/java/ch/njol/skript/conditions/ConditionBlockItemCompatibilityTest.java`
