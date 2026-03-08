# Lane C Status

Last updated: 2026-03-08

## Scope

- Part 1B variables runtime semantics only
- exact upstream-backed closure slice: restore direct-parent `null` sentinel exposure when `Variables.getVariable(...)` reads a list branch that has both a direct value and descendants

## Owned Files

- `src/main/java/ch/njol/skript/variables/Variables.java`
- `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Goal For This Session

- close one contained upstream `Variables` runtime gap without touching parser, statement, classes, config, structures, or canonical docs

## Work Log

- compared local `src/main/java/ch/njol/skript/variables/Variables.java` with upstream snapshot `/tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/variables/Variables.java`
- narrowed the gap to raw list-branch reconstruction in `Variables.getVariable(...)`
- restored the upstream-style `null` sentinel for the direct branch value when a queried list branch exists because descendants are present:
  - `scores::*` now includes `null -> value` when both `{scores}` and descendants like `{scores::group}` exist
  - `scores::group::*` now includes `null -> value` when both `{scores::group}` and descendants like `{scores::group::plain}` exist
- preserved the existing shallow list behavior used by `Variable.getArray(...)` and `getVariablesWithPrefix(...)`
- added focused regressions for both root and nested list branches
- did not claim parity complete
- did not update Stage 8 counts
- did not add new `.sk` fixtures because this parity gap is only observable through the internal `Variables.getVariable(...)` API; it is not surfaced by the current script-facing list variable APIs

## Files Changed

- `src/main/java/ch/njol/skript/variables/Variables.java`
- `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Stage 8 package-local audit counts changed: `0`
- canonical porting doc counts changed: `0`
- Java source file count changed in `src/main/java/ch/njol/skript/variables`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/variables`: `0` added, `1` modified
- real `.sk` fixture count changed: `0`

## Verification

- `./gradlew test --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - passed
  - verified the new root-branch and nested-branch null-sentinel regressions plus the existing variable runtime compatibility suite
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - regression-checked the live runtime, including the existing list-variable `.sk` fixtures
  - suite result: `229 / 229` required tests passed

## Unresolved Risks

- this slice restores internal `Variables.getVariable(...)` branch-map parity only; it does not import the broader upstream `VariablesMap`/storage subsystem
- no new real `.sk` fixture was added because the restored behavior is not observable through the current script API; if a later slice exposes raw list-branch maps to scripts, that surface will need dedicated runtime coverage

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/variables/Variables.java`
  - `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
- no overlap expected with active parser or statement lanes under the current ownership matrix
