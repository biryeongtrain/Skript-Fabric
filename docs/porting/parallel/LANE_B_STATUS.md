# Lane B Status

Last updated: 2026-03-08

## Scope

- `SkriptParser` / `patterns` Part 1A closure work
- parser tag / mark / pattern parity
- parser-facing compatibility tests

## Goal For This Slice

- restore the upstream `SkriptParser.ParseResult.source` metadata on the local compatibility parser path
- keep the change inside Lane B scope without reopening statement/loading orchestration or live `.sk` syntax work
- prove the metadata on both the modern registry parse path and the legacy `parseStatic(...)` path

## Latest Slice

- compared the local `SkriptParser.ParseResult` metadata against `/tmp/skript-upstream-e6ec744-2`
  - upstream `SkriptParser.ParseResult` still carries `@Nullable SkriptPattern source`
  - the local compatibility parser had already restored tags, marks, regex captures, and omitted/default handling, but it was still dropping the matched pattern source metadata before element `init(...)`
- closed the contained parser-metadata gap inside Lane B scope:
  - `ch/njol/skript/lang/SkriptParser.ParseResult` now again exposes `source`
  - `SkriptParser.parseModern(...)` now populates `parseResult.source` with the compiled matched pattern before element initialization
  - `SkriptParser.parseStatic(...)` now does the same for the legacy compatibility path
  - no matcher semantics, keyword prefilter behavior, natural-script whitespace handling, tag ordering, or default-value behavior changed in this slice

## Exact Counts Changed

- local parser-facing tests in `SkriptParserRegistryTest`: `31 -> 33`
- local parser-facing tests in `PatternCompilerCompatibilityTest`: `21 -> 21`
- lane-owned Java files changed in this slice: `2`

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- upstream comparison before the slice:
  - command:
    - `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/SkriptParser.java src/main/java/ch/njol/skript/lang/SkriptParser.java | rg -n "ParseResult|source" -C 2`
  - result:
    - upstream still showed `ParseResult` carrying `@Nullable SkriptPattern source`
    - local `parseModern(...)` / `parseStatic(...)` were constructing `ParseResult` without setting that metadata
- targeted verification after the slice:
  - command:
    - `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - result:
    - passed
    - `build/test-results/test/TEST-ch.njol.skript.lang.SkriptParserRegistryTest.xml`: `33` tests, `0` failures, `0` errors, `0` skipped
    - `build/test-results/test/TEST-ch.njol.skript.patterns.PatternCompilerCompatibilityTest.xml`: `21` tests, `0` failures, `0` errors, `0` skipped

## Coverage Added

- `SkriptParserRegistryTest` now additionally proves:
  - modern effect registration receives the matched compiled pattern through `ParseResult.source`
  - the legacy compatibility `SkriptParser.parseStatic(...)` path also receives the matched compiled pattern through `ParseResult.source`

## Unresolved Risks

- the local compatibility parser still does not implement the fuller upstream `parse_i(...)` / `MatchResult.toParseResult()` flow, so this slice restores only the observable `ParseResult.source` metadata and not broader upstream parser internals
- no live `.sk` parsing surface changed in this slice, so `./gradlew runGameTest --rerun-tasks` was intentionally not run

## Merge Notes

- likely conflict surface is limited to `src/main/java/ch/njol/skript/lang/SkriptParser.java` and `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- this slice does not touch `Statement.java`, `ScriptLoader.java`, `Variables.java`, `Classes.java`, `src/main/java/ch/njol/skript/patterns/**`, or canonical `docs/porting/*.md`
