# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one more upstream-backed `ParserInstance` bridge mismatch: `setCurrentScript(...)` now treats reapplying the same script as a no-op instead of recreating parser hint state
- added a focused regression proving same-script reassignment preserves existing `HintManager` hints

## Files Changed

- `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed upstream returns early when `setCurrentScript(...)` receives the current script, while local still recreated `HintManager`
- `./gradlew test --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- remaining scoped deltas are either larger parser-lifecycle imports or less isolated variable/input bridge questions; avoid widening this slice unless a new self-contained reproducer stays inside `InputSource`, `ParserInstance`, variables, or trigger bridge ownership

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- no canonical docs touched
