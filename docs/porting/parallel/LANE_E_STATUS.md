# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one upstream-backed `ParserInstance` mismatch: `isCurrentEvent(...)` no longer treats subclass-only queries as matching a superclass parse context
- upstream only accepts current events that are equal to or narrower than the requested type
- added a focused regression proving `BaseEvent` does not satisfy `SubEvent`, while `SubEvent` still satisfies `BaseEvent`

## Files Changed

- `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed local `isCurrentEvent(...)` was broader than upstream because it also accepted `current.isAssignableFrom(expected)`
- `./gradlew test --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- remaining `ParserInstance` deltas are either intentionally trimmed API surface or cross into parser/statement/structure ownership and should stay with the coordinator or owning lane

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- no canonical docs touched
