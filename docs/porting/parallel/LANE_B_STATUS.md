# Lane B Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/config/**`
- `src/main/java/ch/njol/skript/util/**`
- `src/main/java/ch/njol/skript/localization/**`
- matching config/util/localization compatibility tests

## Latest Slice

- `StructureType` remains blocked for this worktree because upstream depends on Bukkit `TreeType`; no clean local compile-safe adaptation surfaced, so this slice stayed on fully local util/localization closure
- landed `feat(util): restore chat message parser helpers`
- local `util.chat` now includes upstream-backed `ChatMessages`, adapted to this Fabric branch by keeping chat colors as plain strings instead of Bungee chat colors and by preserving the existing string-backed `MessageComponent` model
- `Utils` now restores the upstream-style `parseHexColor(...)` helper needed for hex tag parsing and legacy `&x`/`§x` color decoding
- expanded `ChatSupportCompatibilityTest` coverage for tag parsing, legacy color parsing, link detection, JSON serialization, style copying, parsed-string decoding, addon chat-code registration, and style stripping
- landed `feat(localization): restore shared numeric parsing helpers`
- `ch.njol.util.StringUtils` now restores upstream-style `numberAfter(...)`, `numberBefore(...)`, and `numberAt(...)`, and `PluralizingArgsMessage` now delegates back to that shared helper instead of its reduced local parser
- expanded `LocalizationCompatibilityTest` coverage for numeric parsing boundaries and pluralization behavior around ordinal-like text
- attempted a primary adapted `Direction` / `WeatherType` / `AABB` util import bundle first, but reverted it after a hard branch-local compile blocker: this worktree has no `org.bukkit.*` compile classpath at all, so even trimmed upstream Bukkit util classes cannot land here yet
- landed `feat(util): restore local chat support leaf types`
- local `util` now includes upstream-backed `chat.LinkParseMode`, `chat.ChatCode`, `chat.MessageComponent`, `chat.SkriptChatCode`, and `chat.package-info`, adapted to this Fabric branch by storing color metadata as plain strings instead of Bungee chat types
- added focused `ChatSupportCompatibilityTest` coverage for color/formatting flags, click and hover payloads, metadata helpers, `MessageComponent.copy()`, and the nullable boolean serializer shape
- landed `feat(util): widen date compatibility helpers`
- `Date` now restores upstream-style `now()`, `fromJavaDate(...)`, timezone construction, mutable `add(...)` / `subtract(...)`, non-mutating `plus(...)` / `minus(...)`, `difference(...)`, `getTime()` / `setTime(...)`, and the deprecated `getTimestamp()` bridge while keeping the local ISO-style string formatting
- added focused `DateCompatibilityTest` coverage for Java-date conversion, timezone offset construction, mutating arithmetic, immutable arithmetic, and difference helpers

## Verification

- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.config.ConfigSupportCompatibilityTest --tests ch.njol.skript.config.ConfigValidationCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --tests ch.njol.skript.util.FileUtilsCompatibilityTest --tests ch.njol.skript.util.ExceptionUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --rerun-tasks` passed
- attempted `./gradlew test --tests ch.njol.skript.util.TimeCompatibilityTest --tests ch.njol.skript.util.StructureTypeCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks`
- result: failed at `:compileJava` because upstream `YggdrasilSerializable` is not present locally and `org.bukkit.*` / `TreeType` are not on this branch's compile classpath
- `./gradlew test --tests ch.njol.skript.util.TimeCompatibilityTest --tests ch.njol.skript.util.ClassInfoReferenceCompatibilityTest --tests ch.njol.skript.util.UtilScaffoldingCompatibilityTest --tests ch.njol.skript.util.EnumUtilsCompatibilityTest --tests ch.njol.skript.util.VersionCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.util.TimeCompatibilityTest --tests ch.njol.skript.util.TimespanCompatibilityTest --rerun-tasks` passed
- attempted `./gradlew test --tests ch.njol.skript.util.DirectionCompatibilityTest --tests ch.njol.skript.util.WeatherTypeCompatibilityTest --tests ch.njol.skript.util.AABBCompatibilityTest --rerun-tasks`
- result: failed at `:compileJava` because this branch currently has no `org.bukkit.*` classes on the compile classpath, so the primary adapted util bundle was reverted instead of partially landed
- `./gradlew test --tests ch.njol.skript.util.chat.ChatSupportCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.util.chat.ChatSupportCompatibilityTest --tests ch.njol.skript.util.DateCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.util.chat.ChatSupportCompatibilityTest --tests ch.njol.skript.localization.LocalizationCompatibilityTest --rerun-tasks` passed

## Next Lead

- next highest-value Lane B follow-up is still `Direction`, but it is now confirmed blocked on the current branch until either a Bukkit-compat utility layer exists locally or the util import is rewritten around non-Bukkit abstractions; until then prefer more fully local `util/**` leaf bundles and deeper `Date` / `localization` / `config` behavior closure that does not pull `org.bukkit.*`, `Delay`, `SkriptTimings`, or `ScriptLoader` executor hooks

## Merge Notes

- likely conflict surface: `src/main/java/ch/njol/skript/config/Config.java`, `src/main/java/ch/njol/skript/localization/Message.java`, `src/main/java/ch/njol/skript/localization/Noun.java`
- new conflicts from this slice should stay isolated to `src/main/java/ch/njol/skript/util/Date.java`, `src/main/java/ch/njol/skript/util/chat/**`, `src/test/java/ch/njol/skript/util/DateCompatibilityTest.java`, and `src/test/java/ch/njol/skript/util/chat/ChatSupportCompatibilityTest.java`
