# Lane C Status

Last updated: 2026-03-09

## Scope

- Variables/Classes/config/structures only
- prioritize classinfo/parser registry parity or deeper variable semantics
- find exactly one upstream-backed mismatch after literal-only `getPatternInfos` closure

## Owned Files

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/main/java/ch/njol/skript/config/**`
- `src/main/java/ch/njol/skript/structures/**`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Goal For This Session

- locate one concrete upstream-backed mismatch in `Classes`/`Variables`/`config`/`structures`
- land a narrow fix with a focused regression and verification commands

## Work Log

- compared local `Node.setKey(...)` / `SectionNode` lookup maintenance with upstream `ch/njol/skript/config/Node#rename` and `SectionNode#renamed`
- mismatch found: renaming a mapped child node updated the node key locally but left `SectionNode`'s case-insensitive lookup map stale, so `get(...)` still resolved the old key and missed the new one
- reproduced via `SectionNodeCompatibilityTest` by renaming an `EntryNode` after `add(...)`
- applied minimal fix: `Node.setKey(...)` now notifies the parent section, and `SectionNode` now refreshes the mapped key through a narrow `renamed(...)` helper like upstream
- compared local `Variables.setVariable(...)` with upstream `ch/njol/skript/variables/Variables#setVariable`
- mismatch found: upstream treats `setVariable("name::*", null, ...)` as list deletion, but the local flat-map bridge only removed the literal `name::*` key and left descendants intact
- reproduced via `VariablesCompatibilityTest` with a direct parent value plus nested descendants under `scores::*`
- applied minimal fix: route `name::*` + `null` through `removePrefix(...)`, which deletes descendants while preserving a direct parent value like upstream `VariablesMap#setVariable(...)`
- compared local `Classes.toString(Object[], boolean)` with upstream `ch/njol/skript/registrations/Classes#toString(Object[], boolean, ...)`
- mismatch found: upstream returns the null sentinel for empty arrays, while the local bridge returned an empty string
- applied minimal fix: empty `Object[]` stringification now delegates to `toString(null, StringMode.MESSAGE)`; added a focused compatibility regression
- compared local `Classes.toString(Object, StringMode)` with upstream `ch/njol/skript/registrations/Classes#toString(Object, StringMode, ...)`
- mismatch found: upstream routes registered legacy parser types through parser-backed string rendering, while the local bridge always fell back to `Object.toString()` and array joins bypassed parser formatting too
- reproduced via `LegacyWrapperCompatibilityTest` with a registered legacy `Parser<LegacyValue>` whose message/debug rendering differs from `record` `toString()`
- applied minimal fix: `Classes.toString(...)` now uses registered legacy parsers for message/debug/variable-name rendering, and `Object[]` joins delegate each element through the same path
- compared local fallback `Classes.toString(Object, StringMode.VARIABLE_NAME)` with upstream `ch/njol/skript/registrations/Classes#toString(Object, StringMode, ...)`
- mismatch found: upstream prefixes unparsed fallback values as `object:...`, while the local bridge returned raw `Object.toString()`
- applied minimal fix: variable-name fallback stringification now returns `object:` + value when no registered parser matches
- compared local `Classes.toString(Object, StringMode)` array handling with upstream `ch/njol/skript/registrations/Classes#toString(Object, StringMode, ...)`
- mismatch found: upstream formats object-typed arrays as bracketed element strings, while the local bridge fell through to Java array identity text
- applied minimal fix: `Classes.toString(Object, StringMode)` now detects arrays up front and recursively formats elements like upstream

## Files Changed

- `src/main/java/ch/njol/skript/config/Node.java`
- `src/main/java/ch/njol/skript/config/SectionNode.java`
- `src/test/java/ch/njol/skript/config/SectionNodeCompatibilityTest.java`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Java source file count changed in `src/main/java/ch/njol/skript/config`: `0` added, `2` modified
- Java test file count changed in `src/test/java/ch/njol/skript/config`: `0` added, `1` modified
- Java source file count changed in `src/main/java/ch/njol/skript/registrations`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/registrations`: `0` added, `1` modified
- real `.sk` fixture count changed: `0`

## Verification

- Repro (before fix): after `node.add(entry); entry.setKey("Beacon");`, `node.get("beacon")` returned `null` and `node.get("marker")` still returned the renamed node
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.config.SectionNodeCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms renamed node keys refresh `SectionNode` lookups like upstream
- Repro (before fix): `Classes.toString("fallback", StringMode.VARIABLE_NAME)` returned `fallback` instead of upstream `object:fallback`
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms upstream variable-name fallback prefix parity
- Repro (before fix): `Classes.toString((Object) new Object[]{"alpha", "beta"}, StringMode.MESSAGE)` returned Java array identity text instead of upstream `[alpha, beta]`
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms object-typed arrays use upstream bracketed element stringification

## Unresolved Risks

- none observed within this narrow surface; broader `Variables`/`Classes` parity remains ongoing

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
