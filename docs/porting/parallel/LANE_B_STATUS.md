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

- mismatch: local `SkriptPattern.match(...)` collapsed all input whitespace before matching, so parser placeholders could receive normalized text instead of the user's exact inner spacing. Upstream trims outer whitespace but does not rewrite placeholder contents.
- minimal fix: matcher input now preserves internal whitespace and only trims the outer edges before keyword prefiltering and regex matching.

## Regression Added

- `ch.njol.skript.lang.SkriptParserRegistryTest`
  - quoted `%string%` placeholder captures now keep internal repeated spaces instead of collapsing them during pattern matching

## Files Changed

- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.lang.SkriptParserRegistryTest' --rerun-tasks`
- results: passed

## Remaining Risks

- this slice only covers matcher whitespace preservation for parser-fed placeholder text
- inline literal-space permissiveness and other matcher edge cases remain unchanged

## Merge Notes

- conflict surface is limited to `SkriptPattern.match(...)`, one parser registry regression, and this lane file
- this slice does not touch `SkriptParser`, loader flow, or class/default registry logic
