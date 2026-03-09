# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one upstream-backed `ParserInstance` bridge mismatch: `setCurrentEvent(...)` and `deleteCurrentEvent()` now notify registered `ParserInstance.Data` listeners via `onCurrentEventsChange(...)`, matching upstream's parser-data event bridge
- added focused regression coverage proving a registered parser-data listener sees the current event classes when set and receives `null` when the event context is cleared

## Files Changed

- `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/upstream-skript/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed upstream dispatches current-event updates to registered parser data, while the local bridge did not
- `./gradlew test --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest`
  - passed

## Next Lead

- remaining scoped deltas are either broader parser-lifecycle imports or larger trigger/runtime behavior changes outside this lane's narrow owned bridge surface; avoid widening unless a new reproducer stays inside `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, or `TriggerSection`

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- no canonical docs touched
