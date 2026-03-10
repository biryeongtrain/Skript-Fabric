# Lane D Status

Last updated: 2026-03-10

## Scope

- `ch/njol/skript/lang/function` facade compatibility + missing `package-info.java` files
- `ch/njol/skript/log` support handlers
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- restored the upstream `org.skriptlang.skript.common.function` core bridge inside lane scope, including common signatures/parameters/arguments/reference runtime plus legacy `Function`/`Signature` integration and registry enumeration support
- added the Java-side `DefaultFunction` builder path with a minimal `Documentable` surface and `Functions.register(DefaultFunction<?>)` overload so more upstream function imports can land without reworking `structures`
- commits:
  - `01fd00330` `feat(log): restore upstream compatibility handlers`
  - `9893b6339` `feat(lang): restore function facade entry points`
  - `d83d69015` `feat(lang): restore function signature parser facade`

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
- upstream reference for this slice: compared against `/tmp/skript-upstream-e6ec744-2/src/main/java/org/skriptlang/skript/common/function/{Function,FunctionArguments,FunctionParser,FunctionReference,Parameter,Parameters,ScriptParameter,Signature,DefaultFunction,DefaultFunctionImpl}.java`, `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/{Function,FunctionRegistry,Functions,Signature}.java`, and `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/doc/Documentable.java`
- `./gradlew test --tests 'ch.njol.skript.lang.function.*' --tests 'ch.njol.skript.lang.parser.*' --tests 'org.skriptlang.skript.common.function.*'`
  - passed

## Next Lead

- next Lane D lead is wiring parser entry points onto the imported common reference parser path once that can be done without broad `SkriptParser` churn; `BukkitLoggerFilter` remains intentionally skipped

## Merge Notes

- likely conflict surface is `src/main/java/ch/njol/skript/lang/function/Functions.java`, `src/main/java/ch/njol/skript/lang/function/FunctionParser.java`, `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`, plus the earlier `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`, `src/main/java/ch/njol/skript/log/SkriptLogger.java`, `src/main/java/ch/njol/skript/log/LogEntry.java`, `src/main/java/ch/njol/skript/log/RedirectingLogHandler.java`, `src/main/java/ch/njol/skript/log/TestingLogHandler.java`, `src/test/java/ch/njol/skript/log/LogHandlerCompatibilityTest.java`, `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`, and the test-only runner shims under `src/test/java/ch/njol/skript/test/runner/`
