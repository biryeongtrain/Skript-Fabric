# Lane A Status

Last updated: 2026-03-09

## Scope

- `classes` + `registrations` + `patterns` parser/type-registry compatibility helpers

## Latest Slice

- restored an upstream-backed registration bundle on top of the earlier enum/doc helper surface
- added `ch/njol/skript/registrations/EventConverter` and `EventValues` against the local event abstraction
- expanded `Feature` into the real experiment-backed compatibility enum and added the owned `registrations/experiments` interfaces `QueueExperimentSyntax` and `ReflectionExperimentSyntax`
- added focused coverage in `EventValuesCompatibilityTest` for exact lookup, conversion-backed lookup, exclude handling, and feature registration wiring

## Verification

- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.registrations.EventValuesCompatibilityTest --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- Lane A safe imports are now mostly blocked on non-owned dependencies:
  - `Serializer`, `EnumSerializer`, `YggdrasilSerializer`, and `ConfigurationSerializer` need `ch.njol.yggdrasil/**`
  - `classes/data/**` is a larger bootstrap bundle that pulls in non-owned runtime and registration dependencies
- next safe Lane A follow-up is whichever remaining `classes` or `registrations` bundle can stay on the local event/runtime abstractions without reopening Bukkit-only registry surface

## Merge Notes

- likely conflict files:
  - `src/main/java/ch/njol/skript/registrations/Feature.java`
  - `src/main/java/ch/njol/skript/registrations/EventValues.java`
  - `src/main/java/ch/njol/skript/registrations/EventConverter.java`
  - `src/test/java/ch/njol/skript/registrations/EventValuesCompatibilityTest.java`
