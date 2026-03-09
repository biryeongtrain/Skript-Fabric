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

- mismatch: when an omitted optional alternation followed an already-matched placeholder, local active-placeholder selection still collapsed to one smallest branch and could skip required defaults from sibling omitted branches.
- minimal fix: when multiple equally small active-placeholder candidates fit the matched expressions, union them instead of picking the first branch so omitted optional alternations keep every required sibling placeholder active.

## Regression Added

- `ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest`
  - validates that omitting an optional alternation still requires defaults for every required placeholder branch, including when an earlier placeholder already matched

## Files Changed

- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/test/java/ch/njol/skript/lang/parser/OmittedPlaceholderRequiredDefaultCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest`
  - `./gradlew test --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest`
- results: passed

## Remaining Risks

- this narrows one omitted-placeholder/default-value edge case only

## Merge Notes

- conflict surface is limited to `SkriptPattern`, one parser compatibility test, and this lane file
- this slice stays within omitted-placeholder/default-value selection and does not touch loader flow
