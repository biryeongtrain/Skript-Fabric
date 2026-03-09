# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one upstream-backed `ParserInstance` parser-data mismatch: `setCurrentScript(...)` now preserves registered `ParserInstance.Data` instances across script-to-script switches and notifies them through upstream-style `onCurrentScriptChange(...)` callbacks before clearing on `null`
- added focused regression coverage proving registered parser-data survives script swaps, sees the new script config, and is still cleared when the parser is deactivated

## Files Changed

- `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/upstream-skript/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed upstream keeps parser-data instances across current-script changes and exposes `ParserInstance.Data.onCurrentScriptChange(...)`, while the local bridge cleared parser-data on every script swap
- `./gradlew test --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest`
  - passed

## Next Lead

- remaining scoped deltas are broader parser-lifecycle imports or larger trigger/runtime behavior changes outside this lane's narrow owned bridge surface; avoid widening unless a new reproducer stays inside `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, or `TriggerSection`

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- no canonical docs touched
