# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one upstream-backed `TriggerItem` mismatch: `walk(...)` now catches `StackOverflowError` and returns `false` instead of letting the trigger bridge tear through the runtime
- kept scope narrow to the runtime bridge only; did not pull in upstream's broader admin-broadcast/reporting path
- extended the focused trigger-bridge regression coverage to prove both ordinary exceptions and direct `StackOverflowError`s fail closed through `walk(...)`

## Files Changed

- `src/main/java/ch/njol/skript/lang/TriggerItem.java`
- `src/test/java/ch/njol/skript/lang/TriggerItemCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/TriggerItem.java src/main/java/ch/njol/skript/lang/TriggerItem.java`
  - confirmed local `walk(...)` still diverged from upstream by rethrowing `StackOverflowError` instead of failing closed
- `./gradlew test --tests ch.njol.skript.lang.TriggerItemCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- remaining scoped deltas are either broader runtime portability choices or already-covered input/parser gaps; avoid widening this slice unless a new upstream-backed reproducer appears

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/TriggerItem.java`
- no canonical docs touched
