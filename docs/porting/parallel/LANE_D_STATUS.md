# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one upstream-backed default-parameter execution mismatch in `ScriptFunction.execute(...)`
- local behavior stored unkeyed plural parameter values at `name::0`, `name::1`, ... inside function-local scope
- upstream-visible list semantics are one-based, and this broke the `8220 function has list default value` shape when a plural parameter fell back to a multi-value default
- `ScriptFunction.execute(...)` now stores unkeyed plural values at `name::1`, `name::2`, ...
- fixed one upstream-backed function runtime mismatch in `FunctionReference.execute(...)`
- local behavior tried to revalidate unresolved references through the reload-only `validateFunction(false)` path, so fresh global references with `script == null` returned `null` instead of lazily binding and executing
- upstream allows those global references to resolve on first execution, so the local path now validates them as first-use calls and binds the function/signature before running
- fixed one execution long-tail mismatch in `FunctionReference.consign(...)`
- local behavior treated any Java array as `Object[]`, which throws on primitive arrays during direct function execution helpers; upstream only unwraps `Object[]` and keeps primitive arrays as single scalar arguments

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/ScriptFunction.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionImplementationCompatibilityTest.java`
- `src/main/java/ch/njol/skript/lang/function/FunctionReference.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Verification

- upstream reference: compared local `src/main/java/ch/njol/skript/lang/function/ScriptFunction.java` against `/tmp/skript-upstream-e6ec744-2/src/test/skript/tests/regressions/8220-function-list-default-value.sk`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionDefaultKeyedParameterCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionOverloadDisambiguationTest`
  - passed
- upstream reference: compared local `src/main/java/ch/njol/skript/lang/function/FunctionReference.java` against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/FunctionReference.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable mismatch in overload selection, keyed/plural execution semantics, or namespace fallback that stays inside `lang/function`

## Merge Notes

- low-conflict slice limited to `FunctionReference.java`, one focused regression, and this lane file
