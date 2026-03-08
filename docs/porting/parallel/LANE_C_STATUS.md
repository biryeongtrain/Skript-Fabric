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

- Continue `Part 1B` after the natural variable-ordering closure.

## Work Log

- closed the next `Classes` registry semantics slice around upstream-style class-info ordering
- `Classes` now computes a stable sorted class-info order that prefers narrower assignable types and honors `before(...)` / `after(...)` dependency hints instead of using raw registration order
- `getSuperClassInfo(...)`, `getClassInfos()`, parser-backed `getPatternInfos(...)`, and `parse(...)` now consume that sorted order
- added unit coverage for:
  - choosing the most specific registered assignable class info for subclass lookups
  - honoring explicit `before(...)` / `after(...)` ordering constraints
- kept this slice inside lane ownership and did not touch canonical docs or parser/statement-owned files
- did not run GameTest because this slice tightened registry-internal compatibility behavior only; no direct user-visible `.sk` runtime path changed in isolation
- did not claim parity complete

## Files Changed

- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Verification

- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - failed
  - Gradle reported `No tests found for given includes: [ch.njol.skript.registrations.ClassesCompatibilityTest](--tests filter)` for the package-private JUnit 5 test class
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks`
  - passed

## Merge Notes

- low conflict surface:
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
