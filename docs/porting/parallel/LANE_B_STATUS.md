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
- pending in worktree: `feat(util): restore lightweight compatibility helpers`
- local `util` now also restores upstream-backed `Getter`, `ValidationResult`, `EmptyStacktraceException`, `Contract`, `Patterns`, and `EnumUtils`, and the tree now includes upstream `package-info.java` files for `config`, `config.validate`, `util`, and `localization`
- added focused compatibility coverage for util scaffolding and enum-localization refresh/fallback behavior

## Verification

- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --rerun-tasks` passed

## Next Lead

- continue inside remaining Lane B scope with the next non-Bukkit-heavy util helpers that do not force cross-lane runtime/addon API edits; `Time` / `Timeperiod` / `Direction` still look like the first classes likely to need wider dependency or runtime decisions

## Merge Notes

- likely conflict surface: `src/main/java/ch/njol/skript/config/Config.java`, `src/main/java/ch/njol/skript/localization/Message.java`, `src/main/java/ch/njol/skript/localization/Noun.java`
- new files are otherwise isolated to `config/package-info.java`, `config/validate/package-info.java`, `localization/package-info.java`, lightweight util helpers under `src/main/java/ch/njol/skript/util/**`, and focused util compatibility tests
