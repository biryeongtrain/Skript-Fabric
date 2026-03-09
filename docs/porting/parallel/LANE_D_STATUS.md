# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one upstream-backed runtime mismatch in `Function.execute(Object[][])`
- local behavior previously treated an explicit empty argument slot (`new Object[0]`) like a missing value and replaced it with a default for ordinary optional parameters
- upstream only evaluates ordinary parameter defaults for `null` slots; an empty provided array stays empty, with keyed defaults remaining the only special-case empty-slot fallback

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/Function.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`

## Verification

- upstream reference: compared local `src/main/java/ch/njol/skript/lang/function/Function.java` against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Function.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable mismatch in overload resolution, namespace fallback, or null-vs-empty runtime edges

## Merge Notes

- low-conflict slice limited to `Function.java`, one focused regression, and this lane file
