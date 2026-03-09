# Lane B Status

Last updated: 2026-03-09

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
- attempted the wider primary-bundle follow-up around `StructureType` / `Direction`, but the current branch does not compile against Bukkit API types (`org.bukkit.*` is absent from the classpath), so that sub-bundle remains blocked without a broader runtime or dependency decision

## Verification

- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --rerun-tasks` passed
- attempted `./gradlew test --tests ch.njol.skript.util.TimeCompatibilityTest --tests ch.njol.skript.util.StructureTypeCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks`
- result: failed at `:compileJava` because upstream `YggdrasilSerializable` is not present locally and `org.bukkit.*` / `TreeType` are not on this branch's compile classpath
- `./gradlew test --tests ch.njol.skript.util.TimeCompatibilityTest --tests ch.njol.skript.util.ClassInfoReferenceCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed

## Next Lead

- continue inside remaining Lane B scope with non-Bukkit util/config/localization helpers that stay fully local to this tree; `Direction`, `StructureType`, and similar Bukkit-backed util imports should stay parked until the coordinator decides whether this branch should gain those runtime dependencies or local compatibility stubs

## Merge Notes

- likely conflict surface: `src/main/java/ch/njol/skript/config/Config.java`, `src/main/java/ch/njol/skript/localization/Message.java`, `src/main/java/ch/njol/skript/localization/Noun.java`
- new conflicts from this slice should stay isolated to `src/main/java/ch/njol/skript/util/{ClassInfoReference,Experience,GameruleValue,Time,Timeperiod}.java` and `src/test/java/ch/njol/skript/util/{ClassInfoReferenceCompatibilityTest,TimeCompatibilityTest}.java`
