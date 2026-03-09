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

## Files Changed

- `src/main/java/ch/njol/skript/variables/Variables.java`
- `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `src/test/java/ch/njol/skript/classes/LegacyWrapperCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Java source file count changed in `src/main/java/ch/njol/skript/variables`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/variables`: `0` added, `1` modified
- real `.sk` fixture count changed: `0`

## Verification

- Repro (before fix): `setVariable("scores::*", null, ...)` left `scores::group` and `scores::group::1` intact instead of deleting the list contents
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.variables.VariablesCompatibilityTest --rerun-tasks`
- Repro (before fix): `Classes.toString(new LegacyValue(7), StringMode.MESSAGE)` returned `LegacyValue[value=7]` instead of legacy parser output `legacy 7`
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --rerun-tasks`
- After fix: targeted commands pass; regressions confirm descendant list deletion parity and legacy parser-backed class stringification parity

## Unresolved Risks

- none observed within this narrow surface; broader `Variables`/`Classes` parity remains ongoing

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/variables/Variables.java`
  - `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
