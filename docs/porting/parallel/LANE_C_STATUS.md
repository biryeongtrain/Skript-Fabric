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

- Continue `Part 1B` after the local variable type-hint infrastructure slice.

## Work Log

- closed the next upstream `variables` gap around local variable type hints
- added `ch/njol/skript/variables/HintManager` with upstream-style scope enter/exit, scope clear/merge, and local-variable hint set/add/remove/delete/get operations
- `ParserInstance` now exposes a dedicated hint manager and resets it on script changes so parse-time scopes do not leak across script/test boundaries
- `Variable.newInstance(...)` now consumes known local hints for simple local variables:
  - generic `%object%` local variables narrow to the hinted runtime type set
  - typed local variables narrow to compatible requested types when hints match
  - incompatible requested types now fail early with a retained variable-type diagnostic instead of silently accepting the wrong type
- added regression coverage proving:
  - hinted local `%object%` variables narrow to the known hinted type
  - hinted local variables reject incompatible requested types
- kept this slice inside the variable/runtime compatibility layer and did not touch canonical docs
- did not claim parity complete: actual parse-time hint producers in loader/effect flow still need to be wired by later lanes
- closed the next `Classes` registry semantics slice around upstream-style class-info ordering
- `Classes` now computes a stable sorted class-info order that prefers narrower assignable types and honors `before(...)` / `after(...)` dependency hints instead of using raw registration order
- `getSuperClassInfo(...)`, `getClassInfos()`, parser-backed `getPatternInfos(...)`, and `parse(...)` now consume that sorted order
- added unit coverage for:
  - choosing the most specific registered assignable class info for subclass lookups
  - honoring explicit `before(...)` / `after(...)` ordering constraints
- explicit literal-pattern matches now also follow that same stable class-info ordering, so shared aliases respect `before(...)` / `after(...)` dependencies instead of falling back to raw registration order
- added a regression proving `getPatternInfos(...)` returns shared literal matches in the same stable order exposed by `getClassInfos()`
- kept this slice inside lane ownership and did not touch canonical docs or parser/statement-owned files
- did not run GameTest because this slice tightened registry-internal compatibility behavior only; no direct user-visible `.sk` runtime path changed in isolation
- did not claim parity complete
- closed the next `Variable` expression-contract gap around list-variable loop aliases and all-values check semantics
- list variables now advertise the legacy loop aliases `var`, `variable`, and `value` in addition to `index`
- `Variable` now restores upstream-style `getAnd()` / `check(...)` behavior so list-variable predicate checks operate on the full value set instead of falling back to single-value/default-expression semantics
- added unit coverage for:
  - legacy loop aliases on list variables
  - list-variable predicate checks requiring all values to satisfy the checker when `getAnd()` semantics apply
- did not run GameTest for this follow-up because it tightened the compatibility-layer expression contract under existing unit coverage; full runtime verification is left to coordinator integration

## Files Changed

- `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/main/java/ch/njol/skript/variables/HintManager.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `src/main/java/ch/njol/skript/lang/Variable.java`
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
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
- `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks`
  - passed after closing explicit literal-pattern ordering parity
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - passed after closing list-variable loop-alias and `check(...)` parity
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - passed after adding local variable type-hint coverage

## Merge Notes

- low conflict surface:
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- current-cycle conflict surface:
  - `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`
  - `src/main/java/ch/njol/skript/lang/Variable.java`
  - `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
