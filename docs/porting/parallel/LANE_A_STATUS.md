# Lane A Status

Last updated: 2026-03-09

## Scope

- `classes` + `registrations` + `patterns` parser/type-registry compatibility helpers

## Latest Slice

- added a Fabric-backed registry compatibility shim under `ch/njol/skript/classes/registry`
- restored `RegistryParser` on top of `net.minecraft.core.Registry`, `Language`, and `MinecraftRegistryLookup`
- restored `RegistryClassInfo` with supplier/parser/default-expression/comparator wiring that stays inside the current local class-registry abstraction
- added a lightweight `RegistrySerializer` id round-trip helper without pulling in `ch.njol.yggdrasil/**`
- added focused coverage in `RegistryCompatibilityTest` for localized lookup, namespaced-id lookup, comparator wiring, and serializer round-trips against built-in item registry entries

## Verification

- `./gradlew test --tests ch.njol.skript.classes.registry.RegistryCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- Lane A now has the registry class-info surface, but the remaining serializer classes are still blocked on non-owned `ch.njol.yggdrasil/**`
- `classes/data/**` remains a larger bootstrap bundle that pulls in non-owned runtime and registration dependencies
- next safe Lane A follow-up is a self-contained `classes`/`registrations` helper import that consumes the new registry shim without requiring the old serializer stack

## Merge Notes

- likely conflict files:
  - `src/main/java/ch/njol/skript/classes/registry/RegistryClassInfo.java`
  - `src/main/java/ch/njol/skript/classes/registry/RegistryParser.java`
  - `src/main/java/ch/njol/skript/classes/registry/RegistrySerializer.java`
  - `src/test/java/ch/njol/skript/classes/registry/RegistryCompatibilityTest.java`
