# Lane C Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/structures/**`
- tightly matching tests only

## Latest Slice

- imported upstream-backed `VariablesMap` and rewired `Variables` to use tree-backed list storage plus deep-copy local scope handoff
- imported `StructVariables` with local-compatible scalar parsing for `variables:` default-value loading
- added focused regressions for `VariablesMap` copy/delete behavior and `StructVariables` default global seeding

## Verification

- command: `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.variables.VariablesMapCompatibilityTest --tests ch.njol.skript.structures.StructVariablesCompatibilityTest --rerun-tasks`
- result: passed

## Next Lead

- wire `StructVariables` into the runtime bootstrap from the coordinator side if live `.sk` coverage is wanted
- continue Lane C with the remaining upstream `variables` bundle (`SerializedVariable`, storage shims) or the next structure import that stays inside owned files

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/variables/Variables.java`
  - `src/main/java/ch/njol/skript/structures/StructVariables.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
