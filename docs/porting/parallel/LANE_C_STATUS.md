# Lane C Status

Last updated: 2026-03-08

## Scope

- Part 1B class-registry compatibility only
- exact upstream-backed closure slice: restore upstream-style recursive array cloning in `ch/njol/skript/registrations/Classes.clone(...)` so keyed function argument consigning does not leak array mutations back to caller-owned values

## Owned Files

- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Goal For This Session

- compare the local `Classes.clone(...)` behavior against upstream snapshot `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/registrations/Classes.java`
- restore the upstream recursive array-clone branch without widening into out-of-scope `ClassInfo` cloner work
- verify the restored behavior both directly through `ClassesCompatibilityTest` and through the live keyed-function argument path that already calls `Classes.clone(...)`

## Work Log

- compared local `src/main/java/ch/njol/skript/registrations/Classes.java` against upstream snapshot `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/registrations/Classes.java`
- confirmed the local `Classes.clone(...)` shim only attempted reflective `clone()` calls and therefore missed the upstream explicit array branch
- confirmed the active runtime already applies `Classes.clone(...)` inside `src/main/java/ch/njol/skript/lang/function/FunctionReference.java` when consigning keyed plural arguments
- restored the upstream recursive array-clone behavior in `Classes.clone(...)`:
  - arrays now allocate a same-component clone
  - each array slot is cloned recursively before assignment
  - non-array behavior stays on the existing reflective `Cloneable` fallback
- added focused direct coverage in `ClassesCompatibilityTest` for nested array cloning
- added applied runtime coverage in `FunctionCallCompatibilityTest` proving keyed function parameters now receive cloned array values and can mutate them without mutating the caller-owned source array
- did not broaden into `variables`, `config`, `structures`, `ClassInfo`, or parser-owned files
- did not add GameTests because this slice changes runtime object isolation inside the existing Java compatibility path, not live `.sk` parsing or structure loading

## Files Changed

- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Stage 8 package-local audit counts changed: `0`
- canonical porting doc counts changed: `0`
- Java source file count changed in `src/main/java/ch/njol/skript/registrations`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/registrations`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/lang/function`: `0` added, `1` modified
- real `.sk` fixture count changed: `0`

## Verification

- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed
  - verified both the direct `Classes.clone(...)` array behavior and the keyed function-argument application path
- `./gradlew test --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed
  - satisfied the lane minimum verification command unchanged

## Unresolved Risks

- this slice restores only the upstream recursive array branch in `Classes.clone(...)`; the broader upstream `ClassInfo`-level cloner surface is still absent locally and remains outside this lane slice
- keyed argument isolation is now correct for array values, but non-array mutable objects without a dedicated clone path still follow the existing shallow behavior

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- no overlap expected with the active parser or statement lanes under the current ownership matrix
