# Lane D Status

Last updated: 2026-03-10

## Scope

- `ch/njol/skript/lang/function` facade compatibility + missing `package-info.java` files
- `ch/njol/skript/log` support handlers
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- restored upstream-style `ErrorDescLogHandler` and `TimingLogHandler`, plus `ch/njol/skript/log/package-info.java`
- extended `LogHandlerCompatibilityTest` to cover error/success boundary logging, out-of-order handler stop semantics, and timing handler elapsed-time tracking
- restored upstream-facing `Functions` facade entry points for `registerFunction(...)`, global `getFunction(...)` / `getSignature(...)`, `getJavaFunctions()`, and no-arg `clearFunctions()`
- added focused function facade regressions and restored the missing `package-info.java` files under `lang`, `lang/function`, `lang/parser`, and `lang/util`
- restored upstream-style `Functions.loadFunction(...)` script-function loading and `ScriptFunction` parameter hint publication during function-body parsing
- extended `FunctionImplementationCompatibilityTest` to prove single and list parameter hints flow into function bodies and that `loadFunction(...)` registers the resulting local implementation
- commits:
  - `01fd00330` `feat(log): restore upstream compatibility handlers`
  - `9893b6339` `feat(lang): restore function facade entry points`
  - pending `fix(function): restore script function loading hints`

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

## Next Lead

- continue upstream diff review for the remaining `lang/function` loader and parser facade APIs, or import another self-contained `log` helper if it does not drag in Bukkit/test-runner-only dependencies

## Merge Notes

- likely conflict surface is `src/main/java/ch/njol/skript/lang/function/Functions.java`, `src/main/java/ch/njol/skript/lang/function/ScriptFunction.java`, `src/test/java/ch/njol/skript/lang/function/FunctionImplementationCompatibilityTest.java`, `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`, `src/test/java/ch/njol/skript/log/LogHandlerCompatibilityTest.java`, and the new `package-info.java` files under `src/main/java/ch/njol/skript/lang/**` plus `src/main/java/ch/njol/skript/log/package-info.java`
