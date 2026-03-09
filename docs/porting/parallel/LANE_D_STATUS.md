# Lane D Status

Last updated: 2026-03-10

## Scope

- `ch/njol/skript/lang/function` facade compatibility + missing `package-info.java` files
- `ch/njol/skript/log` support handlers
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- restored the missing `ch/njol/skript/lang/function/FunctionParser` facade and deprecated `Functions.parseSignature(...)` bridge for future `StructFunction` import work
- aligned the new parser path with local legacy class-name fallbacks plus starred parameter normalization so function signatures can resolve `number` / `numbers` and list-style parameter names without touching `structures`
- extended `FunctionCoreCompatibilityTest` to cover plural return parsing, deprecated facade delegation, starred parameter normalization, and ordinal signature-definition error retention
- commits:
  - `01fd00330` `feat(log): restore upstream compatibility handlers`
  - `9893b6339` `feat(lang): restore function facade entry points`
  - pending `feat(lang): restore function signature parser facade`

## Verification

- upstream reference: compared against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/log/ErrorDescLogHandler.java`, `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/log/TimingLogHandler.java`, `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/log/package-info.java`, `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Functions.java`, and the matching upstream `package-info.java` files under `lang/**`
- `./gradlew test --tests ch.njol.skript.log.LogHandlerCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --rerun-tasks`
  - passed
- upstream reference for this slice: compared against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Functions.java` and `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/ScriptFunction.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed
- upstream reference for this slice: compared against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/log/RedirectingLogHandler.java`, `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/log/TestingLogHandler.java`, and `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `./gradlew test --tests ch.njol.skript.log.LogHandlerCompatibilityTest --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest --rerun-tasks`
  - passed
- upstream reference for this slice: compared against `/tmp/skript-upstream-e6ec744-2/src/main/java/org/skriptlang/skript/common/function/FunctionParser.java`, `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Functions.java`, and `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Signature.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- `log` package count parity is now closed except the intentionally skipped Bukkit-only `BukkitLoggerFilter`; next Lane D lead is the next self-contained parser/function facade closure that reduces `StructFunction` import risk without editing `structures`

## Merge Notes

- likely conflict surface is `src/main/java/ch/njol/skript/lang/function/Functions.java`, `src/main/java/ch/njol/skript/lang/function/FunctionParser.java`, `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`, plus the earlier `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`, `src/main/java/ch/njol/skript/log/SkriptLogger.java`, `src/main/java/ch/njol/skript/log/LogEntry.java`, `src/main/java/ch/njol/skript/log/RedirectingLogHandler.java`, `src/main/java/ch/njol/skript/log/TestingLogHandler.java`, `src/test/java/ch/njol/skript/log/LogHandlerCompatibilityTest.java`, `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`, and the test-only runner shims under `src/test/java/ch/njol/skript/test/runner/`
