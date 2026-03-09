# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/parser` + `ch/njol/skript/log` shared logging compatibility
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- restored upstream-facing parser-owned log handler state through `ParserInstance.getHandlers()` plus new local `HandlerList`
- `SkriptLogger` now uses the active parser instance handler stack instead of a raw thread-local deque, and now exposes upstream-facing `Verbosity`, node bridge, and tracked logging helpers needed by broader imports
- added focused regressions for parser-instance handler isolation, parser-bound log routing, verbosity threshold ordering, and node bridge behavior

## Verification

- upstream reference: compared parser/log API ownership against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`, `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/log/HandlerList.java`, `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/log/Verbosity.java`, and `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/log/SkriptLogger.java`
- `./gradlew test --tests ch.njol.skript.log.LogHandlerCompatibilityTest --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for more mergeable `lang/function` runtime/default-parameter gaps, or import another self-contained `log` support class if it stays lane-local and verifiable

## Merge Notes

- likely conflict surface is `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`, `src/main/java/ch/njol/skript/log/SkriptLogger.java`, the two new `log` support classes, and the focused parser/log compatibility tests
