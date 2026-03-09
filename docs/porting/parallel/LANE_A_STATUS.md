# Lane A Status

Last updated: 2026-03-09

## Scope

- exact upstream-backed `expressions` imports on the existing Fabric runtime

## Latest Slice

- primary attempt on `ExprLastInteractionPlayer` was reverted after focused runtime verification exposed three failing interaction-player scripts
- landed the allowed fallback in the same owned files: exact upstream plural `ExprInteractionDimensions` property forms
- imported the missing user-visible forms through `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java`:
  - `[the] interaction widths of %entities%`
  - `%entities%'[s] interaction widths`
  - `[the] interaction heights of %entities%`
  - `%entities%'[s] interaction heights`
- tightened real `.sk` coverage in `src/gametest/resources/skript/gametest/expression/interaction_dimensions_marks_block.sk` to use those exact plural upstream forms for both set and read paths

## Verification

- `./gradlew runGameTest --rerun-tasks`
  - passed with `230 / 230`

## Next Lead

- re-check other expression families already backed by live Fabric adapters where the remaining gap is exact upstream pattern coverage only

## Merge Notes

- likely conflict file: `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java`
- no canonical docs were touched
