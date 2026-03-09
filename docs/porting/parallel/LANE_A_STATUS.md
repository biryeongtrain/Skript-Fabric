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
- added `DefaultOperations.register()` for a pure-Java arithmetic subset: `Number`, `Timespan`, and `String`
- added `DefaultFunctions.register()` for a pure-Java function subset: numeric math/trig helpers, aggregate number helpers, base conversion, `date`, `calcExperience`, `concat`, and `formatNumber`
- kept the new function/operation slice off Bukkit and off Yggdrasil by resolving required class infos directly from the existing local registry instead of touching `DefaultClasses`
- added focused coverage in `DefaultOperationsCompatibilityTest` and `DefaultFunctionsCompatibilityTest` for arithmetic registration, aggregate/keyed function arguments, base conversion, date construction, formatting, and concatenation

## Verification

- `./gradlew test --tests ch.njol.skript.classes.registry.RegistryCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.classes.data.JavaClassesCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.classes.registry.RegistryCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.classes.data.JavaClassesCompatibilityTest --tests ch.njol.skript.classes.data.DefaultOperationsCompatibilityTest --tests ch.njol.skript.classes.data.DefaultFunctionsCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- Lane A now has both the registry shim and a first local `classes/data` bootstrap subset, but the remaining upstream serializer classes are still blocked on non-owned `ch.njol.yggdrasil/**`
- the remaining `classes/data/**` surface is still mostly Bukkit-bound (`BukkitClasses`, `BukkitEventValues`, most of `DefaultConverters`, most of `DefaultComparators`)
- next safe Lane A follow-up is another pure-Java or registry-backed helper slice inside `classes/data/**`, likely the non-Bukkit portion of `SkriptClasses`, or a small `registrations/**` follow-up that stays off Bukkit and off Yggdrasil
- current blocker inside this bundle: upstream `DefaultOperations` still has `Vector` and `Date +/- Timespan` registrations that cannot be imported cleanly without non-owned `util/Date` work and Bukkit/Fabric vector decisions

## Merge Notes

- likely conflict files:
  - `src/main/java/ch/njol/skript/classes/data/JavaClasses.java`
  - `src/main/java/ch/njol/skript/classes/data/DefaultConverters.java`
  - `src/main/java/ch/njol/skript/classes/data/DefaultComparators.java`
  - `src/main/java/ch/njol/skript/classes/data/DefaultOperations.java`
  - `src/main/java/ch/njol/skript/classes/data/DefaultFunctions.java`
  - `src/test/java/ch/njol/skript/classes/data/JavaClassesCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/classes/data/DefaultOperationsCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/classes/data/DefaultFunctionsCompatibilityTest.java`
  - `src/main/java/ch/njol/skript/classes/registry/RegistryClassInfo.java`
  - `src/main/java/ch/njol/skript/classes/registry/RegistryParser.java`
  - `src/main/java/ch/njol/skript/classes/registry/RegistrySerializer.java`
  - `src/test/java/ch/njol/skript/classes/registry/RegistryCompatibilityTest.java`
