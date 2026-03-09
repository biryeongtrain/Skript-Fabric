# Lane A Status

Last updated: 2026-03-10

## Scope

- `classes` + `registrations` + `patterns` parser/type-registry compatibility helpers

## Latest Slice

- added a new serializer-free `ch/njol/skript/classes/data/**` compatibility subset
- restored `JavaClasses.register()` for pure Java class infos only: `object`, numeric primitives/wrappers, `boolean`, `string`, and `uuid`
- kept the slice off Bukkit and off the removed Yggdrasil stack by using local parsers/default expressions only
- added `DefaultConverters.register()` for the Java-only number subtype converters
- added `DefaultComparators.register()` for the Java-only `Number` comparator with ordering support
- added focused coverage in `JavaClassesCompatibilityTest` for numeric parsing, quoted-string parsing, boolean localization fallback, UUID parsing, converter registration, and comparator behavior

## Verification

- `./gradlew test --tests ch.njol.skript.classes.registry.RegistryCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.classes.data.JavaClassesCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.classes.registry.RegistryCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- Lane A now has both the registry shim and a first local `classes/data` bootstrap subset, but the remaining upstream serializer classes are still blocked on non-owned `ch.njol.yggdrasil/**`
- the remaining `classes/data/**` surface is still mostly Bukkit-bound (`BukkitClasses`, `BukkitEventValues`, most of `DefaultConverters`, most of `DefaultComparators`)
- next safe Lane A follow-up is another pure-Java or registry-backed helper slice inside `classes/data/**` or a small `registrations/**` follow-up that stays off Bukkit and off Yggdrasil

## Merge Notes

- likely conflict files:
  - `src/main/java/ch/njol/skript/classes/data/JavaClasses.java`
  - `src/main/java/ch/njol/skript/classes/data/DefaultConverters.java`
  - `src/main/java/ch/njol/skript/classes/data/DefaultComparators.java`
  - `src/test/java/ch/njol/skript/classes/data/JavaClassesCompatibilityTest.java`
  - `src/main/java/ch/njol/skript/classes/registry/RegistryClassInfo.java`
  - `src/main/java/ch/njol/skript/classes/registry/RegistryParser.java`
  - `src/main/java/ch/njol/skript/classes/registry/RegistrySerializer.java`
  - `src/test/java/ch/njol/skript/classes/registry/RegistryCompatibilityTest.java`
