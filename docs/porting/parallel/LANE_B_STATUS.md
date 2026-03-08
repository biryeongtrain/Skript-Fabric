# Lane B Status

Last updated: 2026-03-09

## Scope

- parser/default-value surface only
- allowed files only:
  - `src/main/java/ch/njol/skript/lang/SkriptParser.java`
  - `src/main/java/ch/njol/skript/lang/parser/DefaultValueData.java`
  - `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
  - `src/test/java/ch/njol/skript/lang/parser/ParserCompatibilityDataAndStackTest.java`
  - `docs/porting/parallel/LANE_B_STATUS.md`

## Goal For This Slice

- close one contained omitted-placeholder/default-value or parser-metadata gap in `SkriptParser`
- stay out of `InputSource`, `Classes`, pattern compilation, and unrelated parser behavior

## Latest Slice

- mismatch: legacy parse path (`SkriptParser.parseStatic`) matched patterns with `PARSE_LITERALS`, silently rejecting expression-only placeholders like `%~type%` and thus blocking registered expressions in legacy syntaxes. Upstream intent is to allow both expressions and literals at the pattern level and rely on per-placeholder flag masks (`%*type%`, `%~type%`, `@time`) to constrain what is accepted.
- minimal fix in parser: switch `parseStatic` to pass `ALL_FLAGS` to the shared matcher (placeholder metadata still narrows behavior). This restores parity with upstream legacy parsing of placeholders and keeps literal/expression restrictions enforced where they belong (on the placeholder).

## Regression Added

- `ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest` (already present from the prior slice)
  - focused parse of single pattern `probe [%string%]` against input `probe`
  - asserts parse fails when no default `string` expression exists (matching upstream)
- `ch.njol.skript.lang.parser.SkriptParserStaticFlagsCompatibilityTest`
  - asserts `parseStatic` accepts a registered expression for `%~integer%` when invoked via a legacy `SyntaxElementInfo`
  - also asserts literals are still rejected for `%~...%` as enforced by the placeholder mask

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/test/java/ch/njol/skript/lang/parser/OmittedPlaceholderRequiredDefaultCompatibilityTest.java`
- `src/test/java/ch/njol/skript/lang/parser/SkriptParserStaticFlagsCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- targeted tests:
  - `./gradlew test --tests 'ch.njol.skript.lang.parser.SkriptParserStaticFlagsCompatibilityTest'`
  - `./gradlew test --tests 'ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest'`
  - `./gradlew test --tests 'ch.njol.skript.lang.SkriptParserRegistryTest'`
- results: all passed

## Remaining Risks

- this slice keeps scope to parser flags and omitted-placeholder behavior only
- broader upstream parity around richer classinfo/default-expression behavior outside omitted-placeholder backfill is still open

## Merge Notes

- conflict surface is limited to the parser/default-value files listed above
- this slice does not touch `InputSource`, `Classes`, or pattern files
