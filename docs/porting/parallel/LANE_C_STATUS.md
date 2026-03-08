# Lane C Status

Last updated: 2026-03-08

## Scope

- stay inside the `lang-core` function runtime/signature/default-parameter parity only
- compare local function resolution behavior against `/tmp/skript-upstream-e6ec744-2`
- find and fix one upstream-visible mismatch in overload resolution within `FunctionRegistry`

## Owned Files

- `src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionOverloadDisambiguationTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Goal For This Session

- verify remaining mismatch in function overload resolution against upstream
- reproduce with a focused regression and land the smallest fix inside `FunctionRegistry`

## Work Log

- diffed `FunctionRegistry` parity against upstream at `/tmp/skript-upstream-e6ec744-2`
- identified a remaining mismatch: upstream prunes ambiguous overloads by preferring candidates whose parameter types exactly equal provided, non-Object argument types; local registry returned AMBIGUOUS in such cases
- reproduced with a focused regression `FunctionOverloadDisambiguationTest` using two overloads `over(Integer)` and `over(Number)` and a call with an `Integer` literal
- implemented the smallest fix: add a tie-breaker in `FunctionRegistry` resolution to filter candidates by exact type equality at positions where the provided type is not `Object`
- kept scope tight (no syntax-import changes; no broader refactors)

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionOverloadDisambiguationTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Java source file count changed in `src/main/java/ch/njol/skript/lang/function`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/lang/function`: `1` added, `0` modified
- real `.sk` fixture count changed: `0`

## Verification

- Repro (failed prior to fix):
  - `./gradlew -q test --no-daemon --console plain --tests "*FunctionOverloadDisambiguationTest"`
  - failed: AMBIGUOUS resolution caused `validateFunction(true)` to return false
- After fix (passes):
  - `./gradlew -q test --no-daemon --console plain --tests "*FunctionOverloadDisambiguationTest"`
  - `./gradlew -q test --no-daemon --console plain --tests "ch.njol.skript.lang.function.*"`
  - both passed; overload selection prefers `over(Integer)` when called with an `Integer` literal

## Unresolved Risks

- tie-breaker mirrors upstream behavior but only applied during ambiguity; other nuanced overload behaviors remain governed by existing matching logic

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
  - `src/test/java/ch/njol/skript/lang/function/FunctionOverloadDisambiguationTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
