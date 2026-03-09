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

- mismatch: local `SkriptPattern.match(...)` trimmed input before keyword prefiltering, so direct compiled-pattern matches accepted leading-whitespace forms that upstream rejects because keywords are checked against the raw input first.
- minimal fix: `SkriptPattern.match(...)` now runs keyword prefiltering on the raw input and only trims once the regex matcher runs, restoring upstream's direct matcher behavior.

## Regression Added

- `ch.njol.skript.patterns.PatternCompilerCompatibilityTest`
  - direct compiled-pattern matches now reject leading-whitespace inputs at keyword prefilter time while still accepting trailing whitespace after trim

## Files Changed

- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.patterns.PatternCompilerCompatibilityTest' --tests 'ch.njol.skript.lang.SkriptParserRegistryTest' --rerun-tasks`
- results: passed

## Remaining Risks

- this slice only covers direct compiled-pattern keyword prefiltering on raw versus trimmed input
- broader matcher and omitted-default parity remain unchanged

## Merge Notes

- conflict surface is limited to `SkriptPattern.match(...)`, one pattern regression, and this lane file
- this slice stays within parser matcher behavior and does not touch loader flow or omitted-default selection
