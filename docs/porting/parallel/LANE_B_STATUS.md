# Lane B Status

Last updated: 2026-03-08

## Scope

- parser/default-value surface only
- allowed files only:
  - `src/main/java/ch/njol/skript/lang/SkriptParser.java`
  - `src/main/java/ch/njol/skript/lang/parser/DefaultValueData.java`
  - `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
  - `src/test/java/ch/njol/skript/lang/parser/ParserCompatibilityDataAndStackTest.java`
  - `docs/porting/parallel/LANE_B_STATUS.md`

## Goal For This Slice

- close one contained omitted-placeholder default-value parity gap inside `SkriptParser`
- stay out of `InputSource`, `Classes`, pattern compilation, and unrelated parser behavior

## Latest Slice

- identified one remaining narrow gap in omitted-placeholder defaults:
  - parser-scoped `DefaultValueData` and classinfo-backed default-expression fallback only resolved exact stored types
  - result: an omitted placeholder such as `%integer%` could not reuse a compatible `Number`-typed parser default or `Number` classinfo default
- restored compatible fallback on the parser/default-value surface:
  - `DefaultValueData.getDefaultValue(...)` now prefers an exact match and otherwise uses the most specific compatible stored type
  - `SkriptParser` now falls back from parser-owned defaults to the most specific compatible registered classinfo default instead of exact-class-only lookup

## Regression Added

- `ParserCompatibilityDataAndStackTest.defaultValueDataUsesMostSpecificCompatibleTypeWhenExactMissing()`
  - proves `DefaultValueData` prefers exact matches but otherwise resolves the most specific compatible stack entry
- `SkriptParserRegistryTest.expressionPatternUsesCompatibleParserAndClassInfoDefaultsForOmittedPlaceholderForm()`
  - registers `default integer value [%integer%]`
  - proves omitted `%integer%` uses a compatible `Number` parser default first, then a compatible `Number` classinfo default once the parser default is removed
  - verifies explicit input still wins

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/lang/parser/DefaultValueData.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `src/test/java/ch/njol/skript/lang/parser/ParserCompatibilityDataAndStackTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- required first narrow command before edits:
  - `./gradlew test --tests 'ch.njol.skript.lang.SkriptParserRegistryTest.expressionPatternUsesClassInfoDefaultValueForExactOmittedPlaceholderForm'`
  - result:
    - passed
- targeted verification after the fix:
  - `./gradlew test --tests 'ch.njol.skript.lang.SkriptParserRegistryTest.expressionPatternUsesCompatibleParserAndClassInfoDefaultsForOmittedPlaceholderForm'`
  - result:
    - passed
  - `./gradlew test --tests 'ch.njol.skript.lang.SkriptParserRegistryTest.expressionPatternUsesCompatibleParserAndClassInfoDefaultsForOmittedPlaceholderForm' --tests 'ch.njol.skript.lang.parser.ParserCompatibilityDataAndStackTest.defaultValueDataUsesMostSpecificCompatibleTypeWhenExactMissing'`
  - result:
    - passed

## Remaining Risks

- this slice only closes compatible-type omitted-placeholder lookup on the parser/default-value surface
- broader upstream parity around richer classinfo/default-expression behavior outside omitted-placeholder backfill is still open

## Merge Notes

- conflict surface is limited to the parser/default-value files listed above
- this slice does not touch `InputSource`, `Classes`, or pattern files
