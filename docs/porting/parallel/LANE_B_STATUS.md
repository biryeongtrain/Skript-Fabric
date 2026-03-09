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

- mismatch: parser-scoped omitted-placeholder defaults were resolved through compatible supertype entries in `DefaultValueData`, so a `%integer%` omission could incorrectly consume a `Number.class` parser default. Upstream only uses exact parser-default type matches here.
- minimal fix in parser data: `DefaultValueData.getDefaultValue(...)` now returns exact-type parser defaults only, so omitted required placeholders no longer succeed through supertype parser defaults.

## Regression Added

- `ch.njol.skript.lang.parser.ParserCompatibilityDataAndStackTest`
  - exact-type lookup now asserts `Integer.class` does not inherit `Number.class` or `Object.class` parser defaults
- `ch.njol.skript.lang.SkriptParserRegistryTest`
  - omitted `%integer%` no longer consumes a parser default registered only for `Number.class`

## Files Changed

- `src/main/java/ch/njol/skript/lang/parser/DefaultValueData.java`
- `src/test/java/ch/njol/skript/lang/parser/ParserCompatibilityDataAndStackTest.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.lang.parser.ParserCompatibilityDataAndStackTest' --tests 'ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest' --tests 'ch.njol.skript.lang.SkriptParserRegistryTest' --rerun-tasks`
- results: passed

## Remaining Risks

- this slice only removes compatible parser-default fallback for omitted placeholders
- compatible classinfo-default fallback for omitted placeholders is still separate and untouched

## Merge Notes

- conflict surface is limited to parser default lookup and two parser-facing tests
- this slice does not touch pattern matching, loader flow, or class registration logic
