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

- mismatch: when an alternation branch was selected by literals alone and that branch omitted one of its own placeholders, omitted-default selection still guessed from present expressions only and could incorrectly require defaults from sibling branches.
- minimal fix: `PatternCompiler` now emits internal branch-activation captures, `SkriptPattern` carries exact matched branch expression indices through `MatchResult`, and `SkriptParser` prefers those exact indices before falling back to pattern-graph inference for omitted-default selection.

## Regression Added

- `ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest`
  - validates that a literal-disambiguated optional alternation branch can use only its own omitted placeholder defaults without requiring sibling-branch defaults, while the existing omitted optional alternation cases still require every matched-branch default

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/patterns/MatchResult.java`
- `src/main/java/ch/njol/skript/patterns/PatternCompiler.java`
- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/test/java/ch/njol/skript/lang/parser/OmittedPlaceholderRequiredDefaultCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest --rerun-tasks`
  - `./gradlew test --tests ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
- results: passed

## Remaining Risks

- branch activation currently scopes exact omitted-default selection only; broader parser tag/mark parity is still open

## Merge Notes

- conflict surface is `SkriptParser`, `PatternCompiler`, `SkriptPattern`, `MatchResult`, one parser compatibility test, and this lane file
- this slice stays within parser/pattern omitted-default selection and does not touch loader flow
