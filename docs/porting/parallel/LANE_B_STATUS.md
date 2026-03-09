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

- mismatch: local `parseStatic(...)` and `parseModern(...)` still attempted pattern matching after trimming blank input, so fully-optional patterns could parse `""` or whitespace-only lines. Upstream rejects blank input before any pattern match.
- minimal fix: both parser entry points now return `null` immediately after trim when the input is blank, preserving the existing `defaultError` logging behavior for non-blank messages only.

## Regression Added

- `ch.njol.skript.lang.parser.SkriptParserBlankInputCompatibilityTest`
  - blank input no longer parses through fully-optional modern or legacy syntax patterns

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/test/java/ch/njol/skript/lang/parser/SkriptParserBlankInputCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.lang.parser.SkriptParserBlankInputCompatibilityTest' --rerun-tasks`
- results: passed

## Remaining Risks

- this slice only covers blank-input gating at the parser entry points
- no additional matcher, tag, or placeholder-default behavior changed

## Merge Notes

- conflict surface is limited to the top-level blank-input guards in `SkriptParser` and one new parser regression test
- this slice does not touch pattern compilation, loader flow, or class/default registry logic
