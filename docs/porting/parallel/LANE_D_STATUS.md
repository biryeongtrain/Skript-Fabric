# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one upstream-backed runtime mismatch in `Function.execute(Object[][])`
- local behavior previously accepted extra argument slots when a function had exactly one declared parameter
- upstream `Function.execute(...)` rejects all over-arity calls and expects single-plural argument condensing to happen earlier in `FunctionReference`
- local failure mode was observable with a one-parameter plural function: `execute(new Object[][]{{"a"}, {"b"}})` leaked both slots into the implementation instead of returning `null`

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/Function.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionImplementationCompatibilityTest.java`

## Verification

- upstream reference: compared local `src/main/java/ch/njol/skript/lang/function/Function.java` against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Function.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest.functionExecuteRejectsOverArityEvenForSinglePluralParameter --rerun-tasks`
  - failed before fix with `ClassCastException`
  - passed after fix
- `./gradlew test --tests 'ch.njol.skript.lang.function.*' --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable mismatch in overload resolution or namespace fallback

## Merge Notes

- low-conflict slice limited to `Function.java`, one focused regression, and this lane file
