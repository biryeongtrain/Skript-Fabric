# Lane B Status

Last updated: 2026-03-09

## Scope

- `SkriptParser` / `patterns` parser-matcher surface only
- allowed files only:
  - `src/main/java/ch/njol/skript/lang/SkriptParser.java`
  - `src/main/java/ch/njol/skript/patterns/**`
  - parser-facing tests
  - `docs/porting/parallel/LANE_B_STATUS.md`

## Goal For This Slice

- close one contained upstream-backed parser/pattern mismatch
- stay out of `InputSource`, `Classes`, loader flow, and unrelated runtime behavior

## Latest Slice

- mismatch: local `SkriptParser` was missing upstream `validatePattern(...)` support entirely, so parser-side validation for user-defined patterns and its specific diagnostics were unavailable.
- minimal fix: restored `SkriptParser.validatePattern(...)` with upstream-style bracket/regex/placeholder validation and normalized placeholder type output.

## Regression Added

- `ch.njol.skript.lang.parser.SkriptParserPatternValidationCompatibilityTest`
  - validates known placeholder normalization and upstream-style pipe-outside-group diagnostics

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/util/NonNullPair.java`
- `src/test/java/ch/njol/skript/lang/parser/SkriptParserPatternValidationCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.lang.parser.SkriptParserPatternValidationCompatibilityTest' --rerun-tasks`
- results: passed

## Remaining Risks

- this slice restores parser-side pattern validation only; it does not change matcher runtime behavior
- placeholder-flag and timed-placeholder validation remain aligned to the current local parser surface, not the full upstream parser grammar

## Merge Notes

- conflict surface is limited to `SkriptParser`, one small utility type, one parser compatibility test, and this lane file
- this slice stays in parser validation and does not touch loader flow or omitted-default selection
