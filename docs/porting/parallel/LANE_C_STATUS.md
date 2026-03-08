# Lane C Status

Last updated: 2026-03-08

## Scope

- `Variables`
- `Classes`
- `config`
- `structures`

## Owned Files

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/main/java/ch/njol/skript/config/**`
- `src/main/java/ch/njol/skript/structures/**`
- matching tests in `src/test/java/ch/njol/skript/**`

## Goal For Next Session

- Continue `Part 1B` after the shallow list-variable read slice.

## Work Log

- compared the current local variable runtime against upstream `e6ec744` and selected one contained `Variables` semantics gap:
  - upstream `VariablesMap.getVariable(...)` plus `Variable.ShallowListProvider` treat `{list::*}` as a shallow read
  - descendant-only entries like `{list::group::1}` do not appear in `{list::*}` unless `{list::group}` itself also has a direct value
  - the local flat-prefix bridge still exposed every descendant key under the prefix, so `{source::*}` incorrectly included nested descendants during list reads and `set {target::*} to {source::*}`
- closed that shallow list-read slice in `Variables.getVariablesWithPrefix(...)`
- `Variables` now returns only direct child entries for list-prefix reads, restoring upstream-style shallow behavior while preserving the existing natural numeric ordering
- added regression coverage proving:
  - `Variable.getArray(...)` / `getArrayKeys(...)` now skip descendant-only entries under the exact source path `scores::group::1`
  - parsed `set {target::*} to {source::*}` now ignores the nested descendant-only source entry `source::group::1` and copies only `source::plain`
- added real `.sk` coverage in `list_variable_shallow_copy_set_test_block.sk`, and the full Fabric GameTest run now completes with `200 / 200` required tests passing
- did not claim parity complete

## Files Changed

- `src/main/java/ch/njol/skript/variables/Variables.java`
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- `src/gametest/resources/skript/gametest/base/list_variable_shallow_copy_set_test_block.sk`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Verification

- `git show e6ec744dd83cb1a362dd420cde11a0d74aef977d:src/main/java/ch/njol/skript/variables/VariablesMap.java | sed -n '130,260p'`
  - passed
  - confirmed upstream list-variable reads come from direct child map nodes in `VariablesMap.getVariable(...)`
- `git show e6ec744dd83cb1a362dd420cde11a0d74aef977d:src/main/java/ch/njol/skript/lang/Variable.java | sed -n '780,860p'`
  - passed
  - confirmed upstream `ShallowListProvider` only emits direct child values, skipping descendant-only entries unless a direct parent slot exists
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - passed
  - exercised the new shallow-read regressions around `scores::group::1`, `source::group::1`, and `source::plain`
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - full Fabric GameTest suite finished with `200 / 200` required tests passing, including the new real `.sk` shallow-copy coverage

## Unresolved Risks

- broader upstream `Variables` parity is still open beyond this slice, especially the missing tree-backed `VariablesMap` / recursive nested-list compatibility surface
- this slice proves the default shallow read path and list-to-list copy behavior, but it does not yet add coverage for cases where a direct parent value and deeper descendants coexist on the same source key

## Merge Notes

- low conflict surface:
  - `src/main/java/ch/njol/skript/variables/Variables.java`
  - `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
  - `src/gametest/resources/skript/gametest/base/list_variable_shallow_copy_set_test_block.sk`
- lane-local status update:
  - `docs/porting/parallel/LANE_C_STATUS.md`
- no cross-lane owned-file overlap was required for this slice
