# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one upstream-backed `TriggerItem` mismatch: `walk(...)` now catches ordinary trigger-time exceptions and returns `false` instead of throwing through the trigger bridge
- kept scope narrow to the runtime bridge only; no stack-overflow handling or broader parser/runtime deltas were pulled in
- added a focused regression proving a throwing trigger item fails closed through `walk(...)`

## Files Changed

- `src/main/java/ch/njol/skript/lang/TriggerItem.java`
- `src/test/java/ch/njol/skript/lang/TriggerItemCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/TriggerItem.java src/main/java/ch/njol/skript/lang/TriggerItem.java`
  - confirmed local `walk(...)` no longer matched upstream's catch-and-return-false behavior for ordinary exceptions
- `./gradlew test --tests ch.njol.skript.lang.TriggerItemCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- remaining scoped deltas are either broader runtime portability choices or already-covered input/parser gaps; avoid widening this slice unless a new upstream-backed reproducer appears

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/TriggerItem.java`
- no canonical docs touched
