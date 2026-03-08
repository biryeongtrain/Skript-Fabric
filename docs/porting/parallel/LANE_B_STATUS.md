# Lane B Status

Last updated: 2026-03-08

## Scope

- `SkriptParser`
- `patterns`
- parser tag / mark parity

## Owned Files

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/patterns/**`
- parser-facing tests in `src/test/java/ch/njol/skript/**`

## Goal For Next Session

- Continue `Part 1A` on richer parser tag / mark / pattern parity without claiming parser-pattern parity complete.

## Work Log

- moved the shared syntax-pattern matcher out of `SkriptParser` and into `ch/njol/skript/patterns`, so parser flow and direct pattern compilation now use the same compiled matcher path
- `SkriptParser.ParseResult` now carries `mark`, and parser init paths now receive general parse tags from matched branches instead of only the prior hardcoded leading `implicit:` case
- `PatternCompiler` / `SkriptPattern` now support compiled literals, placeholders, raw regex captures, optional groups, alternation, general `tag:` metadata, and XOR parse marks via `¦` on the current compatibility surface
- added parser-facing regression coverage for parse marks, branch-specific parse tags, and direct pattern-compiler matching while preserving the already-green inline optional whitespace natural form
- did not mark parity complete: the current slice does not claim full upstream `patterns` element-graph parity, and empty auto-tag forms such as bare `:`-driven tag derivation are still not closed here
- live `.sk` parsing changed through the shared matcher path, so real Fabric GameTest verification was rerun against the existing `.sk` corpus instead of adding a new dedicated `.sk` fixture for currently non-shipping tag/mark syntax

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/patterns/PatternCompiler.java`
- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/main/java/ch/njol/skript/patterns/MatchResult.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`

## Verification

- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - first run failed in `:compileJava` with stale `PatternMatch` references at `src/main/java/ch/njol/skript/lang/SkriptParser.java:307` and `:360`
  - reran after fixing those references; command passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - `195 / 195` required GameTests completed successfully

## Merge Notes

- highest conflict risk is `src/main/java/ch/njol/skript/lang/SkriptParser.java`; Lane A should not have touched it, but coordinator integration work or any out-of-lane parser edits will need manual reconciliation
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java` may conflict with any concurrent parser-regression additions
- `src/main/java/ch/njol/skript/patterns/MatchResult.java` and `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java` are new files in this lane
