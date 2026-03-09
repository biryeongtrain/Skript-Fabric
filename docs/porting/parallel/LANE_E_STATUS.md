# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one more upstream-backed `ParserInstance` / `InputSource` bridge mismatch: `InputSource.parseExpression(...)` now binds the passed parser instance for nested parse work instead of leaking through the ambient thread-local parser
- added a focused regression proving `parseExpression(...)` resolves `input` against the passed parser even when the thread-local parser is carrying a different `InputSource`

## Files Changed

- `src/main/java/ch/njol/skript/lang/InputSource.java`
- `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed the local parser bridge still depended on `ParserInstance.get()` call sites even when `InputSource.parseExpression(...)` was handed a different parser instance
- `./gradlew test --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- remaining scoped deltas are either larger parser-lifecycle imports or trigger-flow differences already outside this narrow bridge fix; avoid widening this slice unless a new self-contained reproducer stays inside `InputSource`, `ParserInstance`, variables, or trigger bridge ownership

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/InputSource.java`
  - `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - `src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java`
- no canonical docs touched
