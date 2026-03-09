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

- mismatch: `TypePatternElement.getCombinations(true)` always returned the raw placeholder text locally, but upstream collapses any non-literal placeholder to `%*%` when building clean pattern combinations. That clean-form parity matters for upstream-style conflict/comparison surfaces.
- minimal fix in patterns: keep literal-only placeholders (`%*type%`) intact for clean combinations, but collapse all other placeholder combinations to `%*%`, matching upstream.

## Regression Added

- `ch.njol.skript.patterns.PatternCompilerCompatibilityTest`
  - added a focused assertion that clean combinations preserve `%-*integer%`
  - added a focused assertion that clean combinations collapse `%strings@1%` to `%*%`

## Files Changed

- `src/main/java/ch/njol/skript/patterns/TypePatternElement.java`
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.patterns.PatternCompilerCompatibilityTest' --rerun-tasks`
- results: passed

## Remaining Risks

- this slice only covers clean combination parity for placeholder pattern elements
- broader matcher/runtime parity in `SkriptParser` and deeper `patterns` backtracking behavior is still open

## Merge Notes

- conflict surface is limited to `TypePatternElement` and the pattern compatibility test
- this slice does not touch loader flow, `InputSource`, or `Classes`
