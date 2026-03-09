# Lane C Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/literals/**`
- `src/main/java/ch/njol/skript/sections/**`
- `src/main/java/ch/njol/skript/structures/**`
- tightly matching tests only

## Latest Slice

- imported the low-dependency upstream literal bundle for numeric boundaries and special constants: `LitDoubleMaxValue`, `LitDoubleMinValue`, `LitFloatMaxValue`, `LitFloatMinValue`, `LitIntMaxValue`, `LitIntMinValue`, `LitLongMaxValue`, `LitLongMinValue`, `LitInfinity`, `LitNegativeInfinity`, `LitNaN`, `LitNewLine`, and `LitPi`
- restored upstream-backed `SerializedVariable` and added the missing owned-package `package-info.java` markers for `variables`, `sections`, `structures`, and `literals`
- added focused parser regressions for literal registration/parsing plus value-object coverage for serialized-variable payloads

## Verification

- command: `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.literals.LiteralsCompatibilityTest --tests ch.njol.skript.variables.SerializedVariableCompatibilityTest --rerun-tasks`
- result: passed

## Next Lead

- continue Lane C with the remaining upstream `variables` bundle only where it stays inside owned files without crossing into Lane B config/util scaffolding
- fallback is the aliases bundle, but it currently looks blocked on broader runtime/item infrastructure outside this lane

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/variables/package-info.java`
  - `src/main/java/ch/njol/skript/variables/SerializedVariable.java`
  - `src/main/java/ch/njol/skript/literals/*`
  - `docs/porting/parallel/LANE_C_STATUS.md`
