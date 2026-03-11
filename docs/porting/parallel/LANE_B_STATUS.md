# Lane B Status

Last condensed: 2026-03-11
Last verified slice date: 2026-03-11

## Scope

- parser/default-value closure only
- primary files:
  - `src/main/java/ch/njol/skript/lang/SkriptParser.java`
  - parser-facing regression tests

## Latest Slice

- Closed one legacy parser-flag mismatch:
  - `SkriptParser.parseStatic(...)` now runs with `ALL_FLAGS`
  - expression-only placeholders like `%~integer%` work again through legacy `SyntaxElementInfo`
  - placeholder masks still enforce literal-versus-expression restrictions

## Regression Added

- `SkriptParserStaticFlagsCompatibilityTest`
- existing omitted-placeholder regression kept green

## Verification

- `./gradlew test --tests 'ch.njol.skript.lang.parser.SkriptParserStaticFlagsCompatibilityTest' --tests 'ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest' --tests 'ch.njol.skript.lang.SkriptParserRegistryTest' --rerun-tasks`
- result: passed

## Remaining Risk

- broader parser default-value parity is still open
- broader legacy parsing parity beyond placeholder flags is still open
