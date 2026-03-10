# Surface F1 Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- tightly matching tests only

## Latest Slice

- added 10 upstream-backed entity-control effects in `ch/njol/skript/effects`:
  - `EffCustomName`
  - `EffEating`
  - `EffHandedness`
  - `EffIgnite`
  - `EffLeash`
  - `EffPlayingDead`
  - `EffShear`
  - `EffTame`
  - `EffToggleCanPickUpItems`
  - `EffMakeFly`
- extended `EffectCompatibilityTest` to register and parse exact upstream syntax branches for the new effect bundle
- extended `ConditionEffectClosureCompatibilityTest` instantiation coverage for the same closure
- attempted `EffPersistent` from the requested primary cluster, but did not land it:
  - current Mojang-mapped local API exposes `Mob.setPersistenceRequired()` as a one-way setter and did not expose a matching clear/unset toggle on the current compatibility surface
  - I dropped the class instead of landing a knowingly one-sided implementation

## Verification

- `./gradlew test --tests ch.njol.skript.effects.EffectCompatibilityTest --tests ch.njol.skript.conditions.ConditionEffectClosureCompatibilityTest`
  - first run failed at compile time:
    - `EffEating`: `Panda.setEating(boolean)` does not exist; local mapping uses `Panda.eat(boolean)`
    - `EffTame`: `TamableAnimal.setTame(...)` requires two boolean arguments
    - `EffShear`: mapped sheep package differs, snow golem import did not resolve on the active classpath
    - `EffPersistent`: local mapped API exposed no clear/unset persistence toggle
  - second run passed after fixing the first three issues and dropping `EffPersistent`

## Next Lead

- stay in nearby effect/entity-control closure before reaching for event/runtime bridge work
- likely next candidates from the same package are the remaining low-dependency entity state effects that do not need scheduler, world, or item-alias glue

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/effects/*.java`
  - `src/test/java/ch/njol/skript/effects/EffectCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/conditions/ConditionEffectClosureCompatibilityTest.java`
