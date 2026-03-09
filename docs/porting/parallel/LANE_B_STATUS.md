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

## Verification

- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed

## Next Lead

- continue inside remaining Lane B scope with `config/package-info.java`, `config/validate/package-info.java`, `localization/package-info.java`, or the next non-Bukkit-heavy util helpers that do not force cross-lane runtime/addon API edits

## Merge Notes

- likely conflict surface: `src/main/java/ch/njol/skript/config/Config.java`, `src/main/java/ch/njol/skript/localization/Message.java`, `src/main/java/ch/njol/skript/localization/Noun.java`
- new files are otherwise isolated to `config/validate/**`, restored localization helpers, `ExceptionUtils`, `FileUtils`, and focused config/util/localization compatibility tests
