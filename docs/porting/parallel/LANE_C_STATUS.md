# Lane C Status

Last condensed: 2026-03-11
Last verified slice date: 2026-03-11

## Scope

- variables/classes/config/structures and adjacent function-runtime dependency closure

## Latest Slice

- Added runtime regression coverage for exact implementation selection:
  - `FunctionOverloadDisambiguationImplementationTest`
  - verifies runtime function binding prefers the exact implementation over a broader overload
- No production code change was needed in this slice because the earlier overload-resolution fix already held.
- `Classes.getPatternInfos(...)` parity is now also restored in coordinator:
  - explicit literal patterns only
  - registration order preserved

## Verification

- `./gradlew test --tests 'ch.njol.skript.lang.function.FunctionOverloadDisambiguationImplementationTest' --tests 'ch.njol.skript.lang.function.*' --rerun-tasks`
- `./gradlew test --tests 'ch.njol.skript.registrations.ClassesCompatibilityTest' --tests 'ch.njol.skript.lang.UnparsedLiteralCompatibilityTest' --rerun-tasks`
- result: passed

## Remaining Risk

- broader function-resolution parity still remains
- broader class-registry compatibility remains thinner than upstream
