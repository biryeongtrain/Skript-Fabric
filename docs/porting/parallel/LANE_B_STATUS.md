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

- remaining upstream-visible mismatch after the compatible omitted-placeholder fallback:
  - behavior: when a required placeholder is omitted via an optional bracket and no default exists for its type, upstream fails the pattern; local port still proceeded to `init(...)` with a null expression
  - example: pattern `probe [%string%]` and input `probe` should fail unless a default `string` expression is available
- minimal fix in parser:
  - `SkriptParser.applyDefaultValues(...)` now returns `null` to signal failure if any non-optional placeholder remains null after default lookup
  - `parseStatic/parseModern` detect this `null` and skip the pattern (matching upstream)

## Regression Added

- `ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest`
  - focused parse of single pattern `probe [%string%]` against input `probe`
  - asserts parse fails (returns null) when no default `string` expression exists (matching upstream)

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/test/java/ch/njol/skript/lang/parser/OmittedPlaceholderRequiredDefaultCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- narrow, focused regression only:
  - `./gradlew test --tests 'ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest'`
  - result: passed

## Remaining Risks

- this slice only closes compatible-type omitted-placeholder lookup on the parser/default-value surface
- broader upstream parity around richer classinfo/default-expression behavior outside omitted-placeholder backfill is still open

## Merge Notes

- conflict surface is limited to the parser/default-value files listed above
- this slice does not touch `InputSource`, `Classes`, or pattern files
