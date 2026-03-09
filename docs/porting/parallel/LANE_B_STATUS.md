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

- mismatch: local omitted-placeholder default diagnostics dropped `NOT_FOUND` classinfo failures whenever another default-expression error existed, so mixed union placeholders only reported the invalid default branch. Upstream keeps every failure reason when no default is usable.
- minimal fix: `SkriptParser.findDefaultValue(...)` now retains missing-default failures in the aggregated upstream-style error message instead of filtering them out.

## Regression Added

- `ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest`
  - omitted `%~number/text%` placeholders now report both the invalid literal default for `number` and the missing default for `text`

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/test/java/ch/njol/skript/lang/parser/OmittedPlaceholderRequiredDefaultCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest' --tests 'ch.njol.skript.lang.SkriptParserRegistryTest' --rerun-tasks`
- results: passed

## Remaining Risks

- this slice only covers aggregated omitted-placeholder default diagnostics for mixed union placeholders
- default selection order and non-diagnostic parser differences remain unchanged

## Merge Notes

- conflict surface is limited to `SkriptParser.findDefaultValue(...)`, one omitted-placeholder regression, and this lane file
- this slice stays within parser default-value behavior and does not touch matcher structure or loader flow
