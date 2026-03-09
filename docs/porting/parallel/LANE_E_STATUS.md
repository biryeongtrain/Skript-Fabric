# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one upstream-backed `ParserInstance` bridge mismatch: switching or clearing the current script now clears transient parser state and parser-scoped data, so stale `InputSource.InputData` does not leak across loads
- kept the slice inside lane-owned bridge state only; did not pull in upstream's broader parser lifecycle, structure, or logging APIs
- added a focused regression proving an active parser's input-source data is dropped when the current script is deactivated

## Files Changed

- `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed the local parser bridge lacked upstream-style transient-state reset around script lifecycle changes
- `./gradlew test --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- remaining scoped deltas are either larger parser-lifecycle imports or already-covered bridge gaps; avoid widening this slice unless a new self-contained reproducer stays inside `InputSource`, `ParserInstance`, `ExprInput`, or trigger bridge ownership

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- no canonical docs touched
