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

- Continue `Part 1A` on richer parser tag / mark / pattern parity after the just-closed empty auto-tag slice, without claiming parser-pattern parity complete.

## Work Log

- moved the shared syntax-pattern matcher out of `SkriptParser` and into `ch/njol/skript/patterns`, so parser flow and direct pattern compilation now use the same compiled matcher path
- `SkriptParser.ParseResult` now carries `mark`, and parser init paths now receive general parse tags from matched branches instead of only the prior hardcoded leading `implicit:` case
- `PatternCompiler` / `SkriptPattern` now support compiled literals, placeholders, raw regex captures, optional groups, alternation, general `tag:` metadata, and XOR parse marks via `¦` on the current compatibility surface
- added parser-facing regression coverage for parse marks, branch-specific parse tags, and direct pattern-compiler matching while preserving the already-green inline optional whitespace natural form
- closed the next parser-owned tag parity gap in `PatternCompiler`: bare leading `:` tags now auto-derive from the following literal token on the current compatibility surface, including literal-leading forms such as `:future` / `:non(-| )` and choice-leading forms such as `:(min|max)[imum]`
- kept empty auto-tags flowing through the same metadata capture path as explicit tags, so derived numeric-looking tags still contribute parse marks while literal-derived tags are normalized to the lowercase branch text that upstream syntax expects
- added direct matcher coverage for literal-derived empty tags, choice-derived empty tags with omitted optional branches, and uppercase literal branches normalizing to lowercase tags
- added a parser-registry regression proving those auto-derived tags reach `SkriptParser.ParseResult` during real syntax-element initialization
- did not mark parity complete: full upstream `patterns` element-graph parity is still open beyond the shared matcher, and this slice only closes the current empty auto-tag derivation gap
- live `.sk` parsing changed through the shared matcher path, so real Fabric GameTest verification was rerun against the existing `.sk` corpus; no new dedicated `.sk` fixture was added because the newly closed empty auto-tag forms are not yet used by the currently shipping local runtime syntax

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
  - prior shared-matcher verification passed with `195 / 195` required GameTests completed successfully
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed after adding empty auto-tag derivation coverage
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed after the empty auto-tag slice
- `./gradlew runGameTest --rerun-tasks`
  - passed after the empty auto-tag slice
  - `196 / 196` required GameTests completed successfully

## Merge Notes

- highest conflict risk is `src/main/java/ch/njol/skript/lang/SkriptParser.java`; Lane A should not have touched it, but coordinator integration work or any out-of-lane parser edits will need manual reconciliation
- current-cycle highest conflict risk is `src/main/java/ch/njol/skript/patterns/PatternCompiler.java`; any concurrent pattern-parity work will need manual reconciliation around the new empty auto-tag helpers
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java` may conflict with any concurrent parser-regression additions
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java` remains a likely conflict point for any concurrent parser-pattern compatibility additions
- `src/main/java/ch/njol/skript/patterns/MatchResult.java` and `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java` are new files from this lane branch
