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

- mismatch: omitted required placeholders still resolved class-info defaults through `Classes.getSuperClassInfo(...)`, so `%integer%` could incorrectly consume a `Number.class` default expression. Upstream only uses the placeholder's exact class-info default here.
- minimal fix in parser default lookup: `SkriptParser.getDefaultValue(...)` now falls back through `Classes.getDefaultExpression(returnType)` instead of superclass class-info lookup, so omitted required placeholders no longer succeed through compatible class-info defaults.

## Regression Added

- `ch.njol.skript.lang.SkriptParserRegistryTest`
  - omitted `%integer%` no longer consumes a class-info default registered only for `Number.class`

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.lang.SkriptParserRegistryTest' --rerun-tasks`
- results: passed

## Remaining Risks

- this slice only removes compatible class-info default fallback for omitted placeholders
- parser-scoped default lookup remains a separate surface from class-info defaults

## Merge Notes

- conflict surface is limited to omitted-placeholder default lookup in `SkriptParser` and one parser-facing registry test
- this slice does not touch pattern compilation, loader flow, or class registration logic
