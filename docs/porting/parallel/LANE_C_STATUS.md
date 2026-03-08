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

- Continue `Part 1B` after the raw list-variable map-read slice.

## Work Log

- compared the current local `Variables` raw-read surface against upstream `e6ec744` and selected one contained compatibility gap:
  - upstream `VariablesMap.getVariable(...)` returns a nested `TreeMap` for `{list::*}` reads
  - when direct parent values and descendants coexist, the parent value is stored under a `null` sentinel key inside that nested map
  - the local flat storage bridge still returned `null` for list-variable reads like `Variables.getVariable("scores::*", ...)`, so the upstream raw list API was missing even though shallow `Variable` reads were green
- closed that raw list-map slice in `Variables.getVariable(...)`
- `Variables` now reconstructs upstream-style nested list maps for list-variable reads from the flat store, preserving:
  - natural numeric ordering for keys like `2` before `10`
  - descendant-only nested maps such as `group -> {1=...}`
  - direct-parent-plus-descendant maps such as `group -> {null=parent, 1=child}`
- `Variables.getVariablesWithPrefix(...)` now derives its shallow direct-child view from that same reconstructed list map, keeping the existing `Variable` runtime path aligned with the raw compatibility surface
- added focused unit coverage proving:
  - `Variables.getVariable("scores::*", ...)` now returns the expected nested map shape for both descendant-only and direct-parent-plus-descendant cases
  - shallow prefix reads still expose only direct child values when a child key also owns deeper descendants
- did not run GameTests because this slice restores the raw compatibility API and does not change user-visible `.sk` syntax or the already-green shallow runtime path
- did not claim parity complete

## Files Changed

- `src/main/java/ch/njol/skript/variables/Variables.java`
- `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Verification

- `git show e6ec744dd83cb1a362dd420cde11a0d74aef977d:src/main/java/ch/njol/skript/variables/VariablesMap.java | sed -n '130,260p'`
  - passed
  - confirmed upstream `{list::*}` reads return nested map nodes from `VariablesMap.getVariable(...)`
- `git show e6ec744dd83cb1a362dd420cde11a0d74aef977d:src/main/java/ch/njol/skript/lang/Variable.java | sed -n '420,520p'`
  - passed
  - confirmed upstream `Variable` consumes those raw nested maps through list providers, with shallow reads using direct child values and direct-parent `null` sentinels
- `./gradlew test --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - passed
  - exercised the new raw-map regressions plus the existing shallow variable behavior after routing prefix reads through the reconstructed nested map

## Unresolved Risks

- broader upstream `Variables` parity is still open beyond this slice, especially the missing native tree-backed `VariablesMap`, recursive nested-list APIs such as iterator support, and queued storage semantics
- this slice reconstructs raw list maps on read from the flat store; it does not yet restore upstream write-time tree maintenance or the remaining local-variable helper surface

## Merge Notes

- low conflict surface:
  - `src/main/java/ch/njol/skript/variables/Variables.java`
  - `src/test/java/ch/njol/skript/variables/VariablesCompatibilityTest.java`
- lane-local status update:
  - `docs/porting/parallel/LANE_C_STATUS.md`
- no cross-lane owned-file overlap was required for this slice
