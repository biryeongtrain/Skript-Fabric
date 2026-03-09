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

## Files Changed

- `src/main/java/ch/njol/skript/variables/Variables.java`
- `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Java source file count changed in `src/main/java/ch/njol/skript/variables`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/variables`: `0` added, `1` modified
- real `.sk` fixture count changed: `0`

## Verification

- Repro (before fix): `setVariable("scores::*", null, ...)` left `scores::group` and `scores::group::1` intact instead of deleting the list contents
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.variables.VariablesCompatibilityTest --rerun-tasks`
- After fix: command above passes; new regression confirms descendant deletion while preserving the direct `scores` value

## Unresolved Risks

- none observed within this narrow surface; broader `Variables`/`Classes` parity remains ongoing

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/variables/Variables.java`
  - `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
