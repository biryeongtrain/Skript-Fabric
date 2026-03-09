# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one upstream-backed overload-resolution mismatch in `FunctionRegistry.resolveRetrieval(...)`
- local behavior greedily filtered ambiguous overloads one argument position at a time, which could incorrectly pick the overload matching the earliest exact argument even when a later exact argument favored a different overload
- upstream evaluates non-`Object` exact positions across the whole candidate before narrowing, so split exact matches remain ambiguous instead of collapsing to the first candidate

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionOverloadDisambiguationTest.java`

## Verification

- upstream reference: compared local `src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java` against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionOverloadDisambiguationTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable mismatch in keyed/default execution semantics or namespace fallback that stays inside `lang/function`

## Merge Notes

- low-conflict slice limited to `FunctionRegistry.java`, one focused regression, and this lane file
