# Lane A Status

Last updated: 2026-03-09

## Scope

- `classes` + `registrations` + `patterns` parser/type-registry compatibility helpers

## Latest Slice

- landed two verified commits:
  - `feat(classes): restore enum compatibility helpers`
  - `chore(classes): restore legacy package metadata`
- restored more upstream-backed owned compatibility surface:
  - added `ch/njol/skript/classes/EnumParser` with the local fallback enum-name mapping needed before localization parity closes
  - added `ch/njol/skript/classes/EnumClassInfo`
  - added `ch/njol/skript/registrations/Comparators` as the legacy bridge over the current comparator backend
  - extended `ClassInfo` with legacy doc/builder hooks (`getName()`, `usage(...)`, `description(...)`, `examples(...)`, `since(...)`, `requiredPlugins(...)`, `documentationId(...)`, `hasDocs()`) and enum auto-supplier parity
  - added `ch/njol/skript/classes/SerializableChecker`
  - restored owned `package-info.java` metadata under `classes`, `registrations`, and `patterns`
- tightened lane tests for enum parsing/class-info wiring, comparator bridge lookup, doc-hook storage, enum auto-suppliers, and the serializable-checker alias

## Verification

- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- Lane A safe imports are now mostly blocked on non-owned dependencies:
  - `Serializer`, `EnumSerializer`, `YggdrasilSerializer`, and `ConfigurationSerializer` need `ch.njol.yggdrasil/**`
  - `EventConverter` / `EventValues` and `classes/registry/**` depend on absent Bukkit event/registry surfaces
  - `classes/data/**` is a larger bootstrap bundle that pulls in non-owned runtime and registration dependencies
- next safe Lane A follow-up is only whichever of those bundles becomes unblocked by coordinator merges; otherwise hand off remaining owned delta as blocked

## Merge Notes

- likely conflict files:
  - `src/main/java/ch/njol/skript/classes/ClassInfo.java`
  - `src/test/java/ch/njol/skript/classes/LegacyWrapperCompatibilityTest.java`
  - `src/main/java/ch/njol/skript/registrations/Comparators.java`
