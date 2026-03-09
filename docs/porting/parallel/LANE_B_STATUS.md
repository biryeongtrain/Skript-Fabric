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

- mismatch: when a choice matched a placeholder-free branch, empty-present active-expression selection still unioned every candidate and could incorrectly require defaults from sibling placeholder branches.
- minimal fix: for empty-present matches, keep only the smallest active-expression candidates and union ties, so placeholder-free choice branches stay self-contained while omitted optional alternations still keep sibling required placeholders active.

## Regression Added

- `ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest`
  - validates that matching a placeholder-free alternation branch does not require defaults from sibling placeholder branches, while omitted optional alternations still require defaults for every required branch

## Files Changed

- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/test/java/ch/njol/skript/lang/parser/OmittedPlaceholderRequiredDefaultCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest`
- results: passed

## Remaining Risks

- this narrows one active-expression/default-selection edge case only

## Merge Notes

- conflict surface is limited to `SkriptPattern`, one parser compatibility test, and this lane file
- this slice stays within omitted-placeholder/default-value selection and does not touch loader flow
