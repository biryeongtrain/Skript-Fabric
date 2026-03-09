# Lane B Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/config/**`
- `src/main/java/ch/njol/skript/util/**`
- `src/main/java/ch/njol/skript/localization/**`
- matching config/util/localization compatibility tests

## Latest Slice

- landed `feat(config): restore validator compatibility helpers`
- `Config` now restores upstream-style reflective loading for nested `OptionSection` trees, and the local tree now includes `config.validate.{NodeValidator,EntryValidator,ParsedEntryValidator,EnumEntryValidator,SectionValidator}` with focused compatibility coverage
- landed `feat(localization): restore message compatibility stack`
- local `localization` now restores `Language`, `LanguageChangeListener`, `Message`, `Noun`, `Adjective`, `ArgsMessage`, `FormattedMessage`, `RegexMessage`, `GeneralWords`, and `PluralizingArgsMessage`, with fallback util support from `ExceptionUtils` and `FileUtils`
- landed `feat(util): restore time and classinfo compatibility helpers`
- local `util` now also restores upstream-backed `Time`, `Timeperiod`, `Experience`, `GameruleValue`, and `ClassInfoReference`, adapted to the local tree where the upstream `YggdrasilSerializable` marker is absent
- added focused compatibility coverage for time parsing/formatting, wrapping time periods, experience/gamerule wrappers, and `ClassInfoReference.wrap(...)` plural-context preservation
- landed `feat(util): widen timespan compatibility support`
- `Timespan` now restores local parsing and formatting helpers for natural-language durations, command short forms, clock forms, `fromDuration(...)`, arithmetic helpers, `TemporalAmount` accessors, and `Timespan.infinite()` / localized `forever` handling without adding new lane-external dependencies
- added focused `TimespanCompatibilityTest` coverage for infinity, localized formatting, arithmetic, temporal conversion, and parsing of natural, short, and clock forms
- the remaining `Direction` import is still open for Lane B, but it was not landed in this batch because the upstream class is a much wider Bukkit-facing util/parser bridge that needs a larger adapted import than this single util-focused slice
- fallback follow-up around `Task` / `AsyncEffect` is blocked on missing current-branch surfaces outside Lane B scope: local `ScriptLoader` has no upstream-style executor hook, and there is no local `ch.njol.skript.effects.Delay` or `ch.njol.skript.timings.SkriptTimings`

## Verification

- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --rerun-tasks` passed
- attempted `./gradlew test --tests ch.njol.skript.util.TimeCompatibilityTest --tests ch.njol.skript.util.StructureTypeCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks`
- result: failed at `:compileJava` because upstream `YggdrasilSerializable` is not present locally and `org.bukkit.*` / `TreeType` are not on this branch's compile classpath
- `./gradlew test --tests ch.njol.skript.util.TimeCompatibilityTest --tests ch.njol.skript.util.ClassInfoReferenceCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.util.TimeCompatibilityTest --tests ch.njol.skript.util.TimespanCompatibilityTest --rerun-tasks` passed

## Next Lead

- next highest-value Lane B follow-up is still `Direction`, but that import now needs a deliberate adapted port plan rather than a small helper drop-in; otherwise continue with any remaining fully local config/localization/util helpers that do not require missing `Delay` / `SkriptTimings` / `ScriptLoader` executor surfaces

## Merge Notes

- likely conflict surface: `src/main/java/ch/njol/skript/config/Config.java`, `src/main/java/ch/njol/skript/localization/Message.java`, `src/main/java/ch/njol/skript/localization/Noun.java`
- new conflicts from this slice should stay isolated to `src/main/java/ch/njol/skript/util/Timespan.java` and `src/test/java/ch/njol/skript/util/TimespanCompatibilityTest.java`
