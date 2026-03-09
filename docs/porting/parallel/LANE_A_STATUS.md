# Lane A Status

Last updated: 2026-03-09

## Scope

- `classes` + `registrations` parser/type-registry compatibility helpers

## Latest Slice

- restored upstream-backed legacy registration helpers in owned files:
  - added `ch/njol/skript/classes/AnyInfo`
  - added `ch/njol/skript/registrations/DefaultClasses` adapted to the local Fabric-backed type classes
  - extended `ClassInfo` with legacy builder/getter hooks for `parser(...)`, `changer(...)`, and `supplier(...)`
  - restored `Classes.registerClass(...)` and `Classes.onRegistrationsStop()`
- tightened lane tests for `AnyInfo` pattern appending, `DefaultClasses` exact-type lookups, and the new `ClassInfo` helper hooks

## Verification

- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue with the remaining import-enabling `classes` surface that does not require Lane B/D ownership, with serializer/enum helpers only if their non-owned dependencies are already present locally

## Merge Notes

- likely conflict files:
  - `src/main/java/ch/njol/skript/classes/ClassInfo.java`
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
