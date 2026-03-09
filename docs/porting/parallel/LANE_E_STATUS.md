# Lane E Status

Last updated: 2026-03-09

## Scope

- owned bridge triage only in `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, and `TriggerSection`
- excluded parser/statement ownership after upstream diff review

## Latest Slice

- fixed one upstream-backed `ParserInstance` section-slice bridge mismatch: restored `getSectionsUntil(...)`, `getSections(int)`, and `getSections(int, Class<? extends TriggerSection>)` so compatibility callers can recover the upstream current-section window helpers again
- added focused regression coverage proving the restored section-slice helpers keep the upstream inclusive depth semantics, reject non-positive depth, and return empty results when no matching section window exists
- fixed one upstream-backed `ExprInput` bridge mismatch: restored the upstream `getSpecifiedType()` accessor so typed `input` expressions expose their registered `ClassInfo` again through the compatibility bridge
- added a narrow regression proving parsed typed inputs retain the exact registered `ClassInfo` on `ExprInput#getSpecifiedType()`
- fixed one upstream-backed `ParserInstance` parser-data mismatch: `setCurrentScript(...)` now preserves registered `ParserInstance.Data` instances across script-to-script switches and notifies them through upstream-style `onCurrentScriptChange(...)` callbacks before clearing on `null`
- added focused regression coverage proving registered parser-data survives script swaps, sees the new script config, and is still cleared when the parser is deactivated
- fixed one upstream-backed `ParserInstance.Data` bridge mismatch: restored the protected `getParser()` accessor alongside the local `parser()` helper so upstream-style parser-data subclasses compile unchanged
- added a narrow regression proving parser-data subclasses can still reach their owning parser through `getParser()`
- fixed one upstream-backed `ParserInstance` section-helper bridge mismatch: restored the current-section lookup/filter helpers so compatibility callers can query the innermost matching section and filtered section lists again
- added a focused regression proving `getCurrentSection(...)`, `getCurrentSections(...)`, and `isCurrentSection(...)` follow upstream subclass-aware section matching

## Files Changed

- `src/main/java/ch/njol/skript/expressions/ExprInput.java`
- `src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java`
- `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Verification

- `diff -u /tmp/upstream-skript/src/main/java/ch/njol/skript/expressions/ExprInput.java src/main/java/ch/njol/skript/expressions/ExprInput.java`
  - confirmed upstream still exposes `ExprInput#getSpecifiedType()` while the local compatibility bridge had dropped that accessor when it flattened typed-input metadata to raw class/name fields
- `./gradlew test --tests ch.njol.skript.lang.InputSourceCompatibilityTest --rerun-tasks`
  - passed
- `diff -u /tmp/upstream-skript/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed upstream keeps parser-data instances across current-script changes and exposes both `ParserInstance.Data.onCurrentScriptChange(...)` and the protected `getParser()` accessor, while the local bridge only kept the non-upstream `parser()` helper
- `./gradlew test --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest`
  - passed
- `diff -u /tmp/upstream-skript/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed the local bridge still lacked the upstream current-section helper accessors (`getCurrentSection(...)`, filtered `getCurrentSections(...)`, and `isCurrentSection(...)`) after the earlier parser-data closures
- `./gradlew test --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest --rerun-tasks`
  - passed
- `diff -u /tmp/upstream-skript/src/main/java/ch/njol/skript/lang/parser/ParserInstance.java src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - confirmed the local bridge still lacked the upstream section-slice helper methods (`getSectionsUntil(...)`, `getSections(int)`, and `getSections(int, Class<? extends TriggerSection>)`) while the remaining `ParserInstance` delta was otherwise broader lifecycle/state surface outside this lane’s mergeable fallback budget
- `./gradlew test --tests ch.njol.skript.lang.parser.ParserInstanceCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- remaining scoped deltas are broader `ParserInstance` lifecycle/state APIs (`backup`, delay state, structure state, logging, experiments, active/inactive orchestration) or larger trigger/runtime behavior changes outside this lane's narrow owned bridge surface; avoid widening unless a new reproducer stays inside `InputSource`, `ParserInstance`, `ExprInput`, `TriggerItem`, or `TriggerSection`

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/expressions/ExprInput.java`
  - `src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java`
  - `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - `src/test/java/ch/njol/skript/lang/parser/ParserInstanceCompatibilityTest.java`
- no canonical docs touched
