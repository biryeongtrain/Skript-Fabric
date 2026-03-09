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
- mismatch: exact active-expression tracking for leading auto-tagged group tokens dropped placeholder indices from the same token's suffix, so a matched `:(...)suffix` branch could skip omitted-default enforcement for required suffix placeholders.
- minimal fix: `PatternCompiler.compileLeadingAutoTaggedGroup(...)` now includes suffix expression indices in both the returned direct-expression set and each emitted branch-activation capture.

## Regression Added

- `ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest`
  - validates that a literal-disambiguated optional alternation branch can use only its own omitted placeholder defaults without requiring sibling-branch defaults, while the existing omitted optional alternation cases still require every matched-branch default
  - validates that a matched leading auto-tagged branch still enforces omitted required defaults from placeholders in the same token suffix

## Files Changed

- `src/main/java/ch/njol/skript/patterns/PatternCompiler.java`
- `src/test/java/ch/njol/skript/lang/parser/OmittedPlaceholderRequiredDefaultCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
- results: passed

## Remaining Risks

- exact branch activation now covers same-token leading auto-tag suffix placeholders too; broader parser tag/mark parity is still open

## Merge Notes

- conflict surface is `PatternCompiler`, one parser compatibility test, and this lane file
- this slice stays within parser/pattern omitted-default selection and does not touch loader flow
