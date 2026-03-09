# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one upstream-backed runtime mismatch in `Function.execute(Object[][])`
- local behavior previously aborted execution when `executeWithNulls` was disabled and a runtime caller passed a `null` parameter slot directly
- upstream only aborts that legacy guard for empty arrays; `null` slots still reach the function body unless a default expression fills them first

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/Function.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`

## Verification

- upstream reference: compared local `src/main/java/ch/njol/skript/lang/function/Function.java` against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Function.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable mismatch in overload resolution, namespace fallback, or `DefaultFunction`-specific runtime edges

## Merge Notes

- low-conflict slice limited to `Function.java`, one focused regression, and this lane file
