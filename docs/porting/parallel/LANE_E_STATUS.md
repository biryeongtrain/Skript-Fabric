# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- primary `ParserInstance` / `InputSource` bridge review found no second mergeable owned mismatch after the already-landed parser-binding fix, so the lane switched to the allowed fallback
- fixed one upstream-backed `TriggerItem` bridge mismatch: `TriggerItem.walk(...)` now rethrows non-`Exception` `Throwable`s instead of silently collapsing them into a `false` return path
- added a focused regression proving `AssertionError` escapes `TriggerItem.walk(...)` while `Exception` and `StackOverflowError` keep their current compatibility handling

## Files Changed

- `src/main/java/ch/njol/skript/lang/TriggerItem.java`
- `src/test/java/ch/njol/skript/lang/TriggerItemCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/TriggerItem.java src/main/java/ch/njol/skript/lang/TriggerItem.java`
  - confirmed the local fallback path no longer matched upstream's distinct handling for `Exception` versus other `Throwable`s
- `./gradlew test --tests ch.njol.skript.lang.TriggerItemCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- remaining scoped deltas are either broader parser-lifecycle imports or larger trigger/runtime behavior changes outside this lane's narrow owned bridge surface; avoid widening unless a new reproducer stays inside `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, or `TriggerSection`

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/TriggerItem.java`
  - `src/test/java/ch/njol/skript/lang/TriggerItemCompatibilityTest.java`
- no canonical docs touched
